package solution;


import provided.StoryTestException;
import java.util.ArrayList;
import java.util.List;

public class StoryTestExceptionImpl extends StoryTestException {
    private String sentence;
    private int numFails;
    private String expected;
    private String actual;

    public StoryTestExceptionImpl() {
        this.sentence = null;
        this.numFails = 0;
        this.expected = null;
        this.actual = null;
    }

    // Setters - return this to enable chaining
    public StoryTestExceptionImpl setSentence(String sentence) {
        this.sentence = sentence;
        return this;
    }

    public StoryTestExceptionImpl setNumFails(int numFails) {
        this.numFails = numFails;
        return this;
    }

    public StoryTestExceptionImpl setExpected(String expected) {
        this.expected = expected;
        return this;
    }

    public StoryTestExceptionImpl setActual(String actual) {
        this.actual = actual;
        return this;
    }

    // Getters - overrides the abstract parent class
    @Override
    public String getSentance() {
        return this.sentence;
    }

    @Override
    public String getStoryExpected() {
        return expected;
    }

    @Override
    public String getTestResult() { return actual; }

    @Override
    public int getNumFail() {
        return this.numFails;
    }

    // Utility function - for our use
    public StoryTestExceptionImpl increaseNumFails(){
        numFails++;
        return this;
    }

}
