package solution;

import org.junit.ComparisonFailure;
import provided.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

public class StoryTesterImpl implements StoryTester {

    private Object objectBackup;

    String firstFailedSentence;
    String expected;
    String result;
    int numFails;

    /** Creates and returns a new instance of testClass **/
    private static Object createTestInstance(Class<?> testClass) throws Exception {
        try {
            // Try constructing a new instance using the default constructor of testClass
            Constructor<?> constructor = testClass.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            // Inner classes case; Need to first create an instance of the enclosing class
            if (testClass.isMemberClass() && !Modifier.isStatic(testClass.getModifiers())) {
                // It's a non-static inner class, needs enclosing instance.
                Class<?> enclosingClass = testClass.getEnclosingClass();
                Object enclosingInstance = createTestInstance(enclosingClass);
                Constructor<?> constructor = testClass.getDeclaredConstructor(enclosingClass);
                constructor.setAccessible(true);
                return constructor.newInstance(enclosingInstance);
            } else {
                // Not an inner class or a static inner class, check for constructors.
                Constructor<?>[] constructors = testClass.getDeclaredConstructors();
                if (constructors.length == 0) {
                    throw new Exception("No constructors available for " + testClass.getName());
                }

                // Prefer a no-argument constructor if available
                for (Constructor<?> constructor : constructors) {
                    if (constructor.getParameterCount() == 0) {
                        constructor.setAccessible(true);
                        return constructor.newInstance();
                    }
                }

                // If no no-argument constructor, use the first available constructor with default values
                Constructor<?> constructor = constructors[0];
                constructor.setAccessible(true);
                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = getDefaultValue(paramTypes[i]);
                }
                return constructor.newInstance(params);
            }
        }
    }

    private static Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            return 0; // Works for byte, short, int, long, float, double
        }
        return null; // Works for all object types
    }

    /** Returns true if c has a copy constructor, or false if it doesn't **/
    private boolean copyConstructorExists(Class<?> c){
        try {
            c.getDeclaredConstructor(c);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /** Assigns into objectBackup a backup of obj.
    /** See homework's pdf for more details on backing up and restoring **/
    private void backUpInstance(Object obj) throws Exception {
        if (obj == null){
            return;
        }
        Object res = createTestInstance(obj.getClass());
        Field[] fieldsArr = obj.getClass().getDeclaredFields();
        for(Field field : fieldsArr){
            field.setAccessible(true);
            Object fieldObject = field.get(obj);
            if (fieldObject == null) {
                field.set(res, null);
                continue;
            }
            Class<?> fieldClass = fieldObject.getClass();

            if(fieldObject instanceof Cloneable && isCloneImplemented(fieldClass)){
                // Case1 - Object in field is cloneable
                try {
                    Method cloneMethod = fieldClass.getDeclaredMethod("clone");
                    cloneMethod.setAccessible(true);
                    Object clonedObject = cloneMethod.invoke(fieldObject);
                    field.set(res, clonedObject);
                } catch (Exception e) {
                    throw new Exception("Cloning failed for cloneable field.", e);
                }
            }
            else if(copyConstructorExists(fieldClass)){
                // Case2 - Object in field is not cloneable but copy constructor exists
                try {
                    Constructor<?> copyConstructor = fieldClass.getDeclaredConstructor(fieldClass);
                    copyConstructor.setAccessible(true);
                    Object copiedObject = copyConstructor.newInstance(fieldObject);
                    field.set(res, copiedObject);
                } catch (Exception e) {
                    throw new Exception("Copy construction failed for the field.", e);
                }
            }
            else{
                // Case3 - Object in field is not cloneable and copy constructor does not exist
                field.set(res, fieldObject);
            }
        }
        this.objectBackup = res;
    }

    /**
     * Checks if the given class or its superclasses implement the clone method.
     * It stops the check once it reaches the java.lang.Object class.
     *
     * @param c The class to check for a clone method implementation.
     * @return true if a clone method is implemented, false otherwise.
     */
    private static boolean isCloneImplemented(Class<?> c) {
        while (c != null && !c.equals(Object.class)) {
            try {
                c.getDeclaredMethod("clone");
                return true;  // Found the clone method in the current class
            } catch (NoSuchMethodException e) {
                // Move up to the superclass to continue the search
                c = c.getSuperclass();
            }
        }
        return false;  // No clone method found in the class hierarchy, up to and excluding Object
    }

    /** Assigns into obj's fields the values in objectBackup fields.
    /** See homework's pdf for more details on backing up and restoring **/
    private void restoreInstance(Object obj) throws Exception{
        // TODO: Remove?
        if (this.objectBackup == null) {
            throw new Exception("Backup object is not initialized.");
        }

        // TODO: Remove?
        // Ensure we're dealing with objects of the same class
        if (!obj.getClass().equals(this.objectBackup.getClass())) {
            throw new Exception("Class types do not match.");
        }

        Field[] classFields = obj.getClass().getDeclaredFields();
        for(Field field : classFields) {
            // Completed.
            field.setAccessible(true);

            // Get the corresponding field from the backup object
            Field backupField = this.objectBackup.getClass().getDeclaredField(field.getName());
            backupField.setAccessible(true);

            // Get the value from the backup and assign it to the current object's field
            Object value = backupField.get(this.objectBackup);
            field.set(obj, value);
        }
    }

    /** Returns the matching annotation class according to annotationName (Given, When or Then) **/
    private static Class<? extends Annotation> GetAnnotationClass(String annotationName){
        return switch (annotationName) {
            // Return matching annotation class
            case "Given" -> Given.class;
            case "When" -> When.class;
            case "Then" -> Then.class;
            default -> null;
        };
    }

    private String getAnnotationValue(Method m, Class<?> AnnoType) {
        if (AnnoType.equals(Given.class)) {
            Given annotation = m.getAnnotation(Given.class);
            if (annotation == null) return null;
            return annotation.value();
        }
        if (AnnoType.equals(When.class)) {
            When annotation = m.getAnnotation(When.class);
            if (annotation == null) return null;
            return annotation.value();
        }
        if (AnnoType.equals(Then.class)) {
            Then annotation = m.getAnnotation(Then.class);
            if (annotation == null) return null;
            return annotation.value();
        }
        return null;
    }

    private Method findMethod(Class<?> testClass, Class<? extends Annotation> annotationClass, String sentenceSub)
                    throws WordNotFoundException {

        for (Method method : testClass.getDeclaredMethods()) {
            String value = getAnnotationValue(method, annotationClass);
            if (value == null)
                continue;

            if (sentenceSub.equals(value.substring(0, value.lastIndexOf(" &")).trim())) {
                return method;
            }
        }
        return null; // the right method does not exist in testClass
    }
    private Method searchMethod(Class<?> testClass, Class<? extends Annotation> annotationClass, String sentenceSub)
            throws WordNotFoundException {
        if (testClass == null) {
            if (Given.class.equals(annotationClass)) {
                throw new GivenNotFoundException();
            } else if (When.class.equals(annotationClass)) {
                throw new WhenNotFoundException();
            } else if (Then.class.equals(annotationClass)) {
                throw new ThenNotFoundException();
            } else {
                throw new WordNotFoundException(); // should not reach here if annotationClass is always one of the expected types
            }
        }
        Method method = findMethod(testClass, annotationClass, sentenceSub);
        // recursive call towards parent:
        if(method == null)
            return searchMethod(testClass.getSuperclass(), annotationClass, sentenceSub);
        return method;
    }

    private void invokeAnnotatedMethod(Method method, Object instance, String parameterValue)
            throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        Class<?> paramType = method.getParameterTypes()[0]; // each method has 1 param
        Object castedParam = (paramType == Integer.class || paramType == int.class) ? Integer.parseInt(parameterValue) : parameterValue;
        method.invoke(instance, castedParam);
    }

    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if(story == null || testClass == null)
            throw new IllegalArgumentException();

        this.numFails = 0;
        Object testInstance = createTestInstance(testClass);
        boolean firstWhenEncountered = false; // Added

        for(String sentence : story.split("\n")) {
            boolean methodFound = false;
            String[] words = sentence.split(" ", 2);

            String annotationName = words[0];
            Class<? extends Annotation> annotationClass = GetAnnotationClass(annotationName);

            String sentenceSub = words[1].substring(0, words[1].lastIndexOf(' ')); // Sentence without the parameter and annotation
            String parameter = sentence.substring(sentence.lastIndexOf(' ') + 1);

            // Completed.
            Method method = searchMethod(testClass, annotationClass, sentenceSub); // Will throw the appropriate exception if needed

            if (annotationClass == Given.class){
                invokeAnnotatedMethod(method, testInstance, parameter); // call invoke - shouldn't fail
            }
            else if(annotationClass == When.class) {
                if (!firstWhenEncountered) { // backup before executing all the "when" statements
                    backUpInstance(testInstance);
                    firstWhenEncountered = true;
                }
                invokeAnnotatedMethod(method, testInstance, parameter); // call invoke - shouldn't fail
            }
            else { // annotationClass == Then.class
                try {
                    firstWhenEncountered = false;
                    invokeAnnotatedMethod(method, testInstance, parameter);
                }
                catch (IllegalAccessException e){ // shouldn't get here
                    restoreInstance(testInstance); // restore
                }
                catch (InvocationTargetException e){
                    if (e.getCause().getClass() == org.junit.ComparisonFailure.class) {
                        if (this.numFails == 0) {
                            ComparisonFailure ex = (org.junit.ComparisonFailure) e.getCause();
                            this.firstFailedSentence = sentence;
                            this.expected = ex.getExpected();
                            this.result = ex.getActual();
                        }
                        this.numFails++;
                    }
                    restoreInstance(testInstance); // restore
                }
            }
        }
        // Throw StoryTestExceptionImpl if the story failed.
        if (this.numFails > 0){
            StoryTestExceptionImpl e = new StoryTestExceptionImpl();
            e.setSentence(this.firstFailedSentence);
            e.setNumFails(this.numFails);
            e.setActual(this.result);
            e.setExpected(this.expected);
            throw e;
        }

    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        // Completed.
        if (story == null || testClass == null)
            throw new IllegalArgumentException();
        try {
            String sentence = story.substring(0, story.indexOf("\n"));
            String[] words = sentence.split(" ", 2);
            String sentenceSub = words[1].substring(0, words[1].lastIndexOf(' ')); // Sentence without the parameter and annotation
            searchMethod(testClass, Given.class, sentenceSub); // Will throw if it can't find method for Given
            testOnInheritanceTree(story, testClass); // Found method, OK to test on this testClass
        } catch (GivenNotFoundException e) { // didn't find in current class
            Class<?>[] subClasses = testClass.getDeclaredClasses();
            // Go over all nested classes
            for (Class<?> subClass : subClasses) {
                testOnNestedClasses(story, subClass);
            }
        }
    }
}
