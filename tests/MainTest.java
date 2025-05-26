import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {



    @Test
    void testProperName() {
        Boolean [] expected = new Boolean[] {true, false, true, true};
        String [] testStrings = new String[] {"Bob", "Bo", "Smith", "Joey"};
        for (int i = 0; i < testStrings.length; i++) {
            assertEquals(Main.properName(testStrings[i]), expected[i]);
        }
    }

    @Test
    void testInt() {
        Boolean [] expected = new Boolean[] {true, true, true, true, true, true, false};
        String[] testStrings = new String[]{"12", "43.23", "-34.5", "+98.7", "0", "0.0230", "023"};
        for (int i = 0; i < testStrings.length; i++) {
            assertEquals(Main.integer(testStrings[i]), expected[i]);
        }
    }

    @Test
    void testAncestor() {
        Boolean [] expected = new Boolean[] {true, true, true, true, true, false, true, false, true};
        String[] testStrings = new String[] {
                "father",
                "Mother",
                "great-great-grandmother",
                "GrandFather",
                "great-grandfather",
                "father-inlaw", // false
                "great-great-great-great-grandmother",
                "great-father", // false
                "great-grandparent"
        };

        for (int i = 0; i < testStrings.length; i++) {
            assertEquals(Main.ancestor(testStrings[i]), expected[i]);
        }
    }

    @Test
    void testPalindrome() {
        assertTrue(Main.palindrome("asdfggfdsa"));
        assertTrue(Main.palindrome("asdFggfdSa"));
        assertFalse(Main.palindrome("asdffdsa"));
        assertFalse(Main.palindrome("raceriecar"));
    }

    @Test
    void testWordleMatches() {
        List<List<WordleResponse>> wordleResponse = new ArrayList<>();

        // first response
        List<WordleResponse> firstResponse = new ArrayList<>();
        // "brain"
        firstResponse.add(new WordleResponse('b', 0, LetterResponse.WRONG_LETTER));
        firstResponse.add(new WordleResponse('r', 1, LetterResponse.WRONG_LOCATION));
        firstResponse.add(new WordleResponse('a', 2, LetterResponse.WRONG_LETTER));
        firstResponse.add(new WordleResponse('i', 3, LetterResponse.WRONG_LETTER));
        firstResponse.add(new WordleResponse('n', 4, LetterResponse.CORRECT_LOCATION));

        wordleResponse.add(firstResponse);

        // second response
        List<WordleResponse> secondResponse = new ArrayList<>();
        // "thorn"
        secondResponse.add(new WordleResponse('t', 0, LetterResponse.WRONG_LETTER));
        secondResponse.add(new WordleResponse('h', 1, LetterResponse.WRONG_LETTER));
        secondResponse.add(new WordleResponse('o', 2, LetterResponse.CORRECT_LOCATION));
        secondResponse.add(new WordleResponse('r', 3, LetterResponse.CORRECT_LOCATION));
        secondResponse.add(new WordleResponse('n', 4, LetterResponse.CORRECT_LOCATION));

        wordleResponse.add(secondResponse);

        List<String> result = Main.wordleMatches(wordleResponse);
        assertEquals(3, result.size());
        for (String res : result)
            System.out.println(res);
    }

    @Test
    void testGenSet() {
        Set<Character> treeSet = new TreeSet<>();
        treeSet.add('k');
        treeSet.add('c');
        treeSet.add('f');
        treeSet.add('g');

        assertEquals("[a-bd-eh-jl-z]", Main.generateSet(treeSet).toString()); // expect set cutting
        assertEquals("[a-z]", Main.generateSet(null).toString()); // by default, we need a valid set of the alphabet
    }

    @Test
    void testGetRegex() {
        List<List<WordleResponse>> wordleResponse = new ArrayList<>();

        // first response
        List<WordleResponse> firstResponse = new ArrayList<>();
        // "CRAZE"
        firstResponse.add(new WordleResponse('b', 0, LetterResponse.CORRECT_LOCATION));
        firstResponse.add(new WordleResponse('r', 1, LetterResponse.WRONG_LOCATION));
        firstResponse.add(new WordleResponse('a', 2, LetterResponse.WRONG_LETTER));
        firstResponse.add(new WordleResponse('i', 3, LetterResponse.WRONG_LOCATION));
        firstResponse.add(new WordleResponse('n', 4, LetterResponse.WRONG_LETTER));

        wordleResponse.add(firstResponse);

        assertEquals("^(?=[a-z]*i)(?=[a-z]*r)(?!.*[an])b[a-qs-z][a-z][a-hj-z][a-z]$", Main.getRegex(wordleResponse).toString());
    }

}