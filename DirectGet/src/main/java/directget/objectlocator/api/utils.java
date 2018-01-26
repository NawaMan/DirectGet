package directget.objectlocator.api;

class utils {
    
    /**
     * Find the {@code java.inject.Inject} class by name.
     * @return the class if found or {@code null} if not.
     */
    static Class<?> findClass(String name) {
        try {
            // TODO - make it support inner class
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
}
