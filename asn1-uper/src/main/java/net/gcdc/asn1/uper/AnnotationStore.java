package net.gcdc.asn1.uper;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class AnnotationStore {

    private Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

    public AnnotationStore(Annotation[] classAnnot, Annotation[] fieldAnnot) {
        for (Annotation a : classAnnot) {
            annotations.put(a.annotationType(), a);
        }
        for (Annotation a : fieldAnnot) {
            annotations.put(a.annotationType(), a);
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> classOfT) {
        @SuppressWarnings("unchecked")
        // Annotations were added with value T for key classOfT.
        T result = (T) annotations.get(classOfT);
        return result;
    }

    public Collection<Annotation> getAnnotations() {
        return annotations.values();
    }
}