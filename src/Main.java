    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.util.*;
    import java.util.regex.Pattern;

    class WordleResponse {
        char c;
        int index;

        @Override
        public String toString() {
            return "WordleResponse{" +
                    "c = " + c +
                    ", index = " + index +
                    ", resp = " + resp +
                    '}';
        }

        LetterResponse resp;

        public WordleResponse(char c, int index, LetterResponse resp) {
            this.c = c;
            this.index = index;
            this.resp = resp;
        }
    }

        enum LetterResponse {
            CORRECT_LOCATION, // Green
            WRONG_LOCATION,   // Yellow
            WRONG_LETTER      // Gray
        }

        public class Main {

            static boolean properName(String s) {
                return match(Pattern.compile("[A-Z][a-z]{2,}"), s);
            }// matches a proper name like Bob, Smith, Joey

            static boolean integer(String s) {
                return match(Pattern.compile("^[-+]?(?!0\\d)\\d*\\.?\\d*"), s);
            }//a number (integer or decimal, positive or negative) 12, 43.23, -34.5, +98.7, 0, 0.0230 (but not 023)

            static boolean ancestor(String s) {
                 return match(Pattern.compile("^((great-)*(grand)|(grand)?)(father|mother|parent)$",
                         Pattern.CASE_INSENSITIVE), s);
            } // an ancestor like father, mother, great-great-grandmother.

            static boolean palindrome(String s) {
                if (s.length() != 10) // must be of size 10
                    return false;

                //create the regex string
                String regex = s.substring(0, s.length() / 2);

                //add reverse
                StringBuilder reverse = new StringBuilder();
                for (int i = regex.length() - 1; i >= 0; i--) {
                    reverse.append(regex.charAt(i));
                }
                regex += reverse;

                return match(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), s);
            } // a 10 letter case insensitive palindrome like "asdfggfdsa"


            // we need to cut the set (e.g. if letter 'e' can't be in position then set: [a-df-z]. Note e is cut out
        static StringBuilder generateSet(Set<Character> set) {
            // set must be defined
            if (set == null)
                return new StringBuilder("[a-z]");

            String alphabet = "abcdefghijklmnopqrstuvwxyz";

            StringBuilder regexResponse = new StringBuilder("[");
            if (!set.contains('a'))
                regexResponse.append("a-");

            int index, prevIndex = -1;
            for (Character c : set)
            {
                // get index
                index = alphabet.indexOf(c);
                // append before cut char (if not a)
                if (c != 'a')
                {
                    // if we should remove after cut from previous (if previous is a letter behind)
                    // e.g. Let's say the set should exclude: a,m,n,z then we need a set of: [b-lo-y] and not: [b-ln-mo-y] which we need to take out n-m (because they are next to each other)
                    if (prevIndex == index - 1)
                        regexResponse.delete(regexResponse.length() - 2, regexResponse.length()); // delete last 2 chars (of previous: appended after cut)
                    else
                        regexResponse.append(alphabet.charAt(index - 1));
                }
                // append after cut char (if not z)
                if (c != 'z')
                    regexResponse
                            .append(alphabet.charAt(index + 1))
                            .append((c == 'y' ? "" : "-")); // since y is excluding z (...-y) which is at the end of the alphabet we can't have a "-" after
                prevIndex = index;
            }
            // append z
            if (!set.contains('z'))
                regexResponse.append("z");
            regexResponse.append(']');

            return regexResponse;
        }

            static StringBuilder getRegex(List<List<WordleResponse>> wordleResponse) {
                // Organize data from Wordle Responses

            // wrong letters should not have duplicates
            Set<Character> wrongLetters = new HashSet<>();
            // ArrayList to get access to the contains method.
            List<Character> correctLocation = new ArrayList<>(Collections.nCopies(5, '\0')); // char: '\0' same as null
            Set<Character>[] wrongLocation = new Set[5];
            // we need for positive lookaheads
            // (TreeSet) Ordered for set cutting (the alphabet)
            Set<Character> nonDuplicates = new TreeSet<>();

                for (List<WordleResponse> responses : wordleResponse) {

                    for (WordleResponse response : responses) {
                        // print the response
                        System.out.printf("Letter '%c' at index %d is marked as %s%n", response.c, response.index, response.resp);
                        switch (response.resp) {
                            // add correct location letter in its index location
                            case CORRECT_LOCATION ->
                                    correctLocation.set(response.index, response.c);
                            case WRONG_LOCATION ->
                            {
                                // create set if null
                                if (wrongLocation[response.index] == null)
                                    wrongLocation[response.index] = new TreeSet<>();

                                // add char to wrongLocation at index
                                wrongLocation[response.index].add(response.c);
                                // add char to nonDuplicates
                                nonDuplicates.add(response.c);
                                System.out.println("Yellow letter '" + response.c + "' excluded from position " + response.index);
                            }
                            case WRONG_LETTER -> wrongLetters.add(response.c);
                        }
                    }
                }
            // generate regex
            StringBuilder regex = new StringBuilder("^");

            // generate positive lookaheads (note: 's') (wrong Location letters)
            for (Character c : nonDuplicates) {
                regex.append(String.format("(?=[a-z]*%c)", c));
            }

            // generate negative lookahead (wrong letters)
            regex.append("(?!.*[");
            for (char c : wrongLetters) {
                // bug when wrong letters contains any correct letters. (since lookahead looks through all 5 letters)
                if (!correctLocation.contains(c))
                    regex.append(String.format("%s", c));
            }
            regex.append("])");

            // generate the sets of the 5 letters: [a-z] {5} // we can have for example: [a-df-su-z] to avoid e, and t
                for (int i = 0; i < 5; i++) { // loop through correct and wrongLocation letter
                    // if correct letter is not null (overrides set creation)
                    if (correctLocation.get(i) != '\0')
                        regex.append(correctLocation.get(i));
                    else {// generate the set cutting out wrong Location letters (of position)
                        //regex.append(generateSet(wrongLocation[i]));
                        Set<Character> excluded = wrongLocation[i] == null ? Collections.emptySet() : wrongLocation[i];
                        System.out.println("Position " + i + " excludes: " + excluded);
                        regex.append(generateSet(excluded));
                    }
                }
                regex.append('$');

            return regex;
            }

            static List<String> wordleMatches(List<List<WordleResponse>> list) {
                List<String> words = new ArrayList<>(100);

                try {
                    //get the file of words
                    String path = new File(".").getCanonicalPath();
                    File wordsFile = new File(path + "/src/5LetterWords.txt");

                    Scanner scanner;

                    try {
                        scanner = new Scanner(wordsFile);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    //get the regex
//                    Pattern pattern = Pattern.compile(getRegex(list).toString(), Pattern.CASE_INSENSITIVE);
//                    System.out.println(getRegex(list).toString());
                    String regexStr = getRegex(list).toString();
                    System.out.println("Generated regex: " + regexStr);

                    Pattern pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);


                    String nextLine;

                    while (scanner.hasNextLine()) {
                        nextLine = scanner.nextLine();

                        //test the word to see if it matches the regex
                        if (match(pattern,nextLine))
                            words.add(nextLine);
                    }

                    return words; // returns valid words that match the regex

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } // returns all matches for the previous wordle

            static boolean match(Pattern pattern, String s) {
                return pattern.matcher(s).find();
            }

            public static void main(String[] args) {
                System.out.println("Hello world!");
            }


        }
