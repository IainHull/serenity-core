package net.thucydides.core.requirements.classpath;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import net.thucydides.core.annotations.Narrative;
import net.thucydides.core.requirements.annotations.NarrativeFinder;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by john on 20/07/2016.
 */
public abstract class ClassNarrative {
    public static ClassNarrative text() {
        return new ClassNarrativeText();
    }

    public static ClassNarrative type() {
        return new ClassNarrativeType();
    }

    public abstract Optional<String> definedInPath(String path);

    protected Optional<Narrative> getClassLevelNarrativeFor(String path) {
        try {
            return NarrativeFinder.forClass(getClass().getClassLoader().loadClass(path));
        } catch (ClassNotFoundException e) {
            return Optional.absent();
        }
    }

    public static class ClassNarrativeText extends ClassNarrative {
        public Optional<String> definedInPath(String path) {
            Optional<Narrative> narrative = getClassLevelNarrativeFor(path);
            if (narrative.isPresent() && !isEmpty(getNarrativeTextBody(narrative.get()))) {
                return Optional.of(getNarrativeTextBody(narrative.get()));
            }
             return Optional.absent();
        }
    }

    private static String getNarrativeTextBody(Narrative narrative) {
        return Joiner.on(System.lineSeparator()).join(narrative.text());
    }

    public static class ClassNarrativeType extends ClassNarrative {
        public Optional<String> definedInPath(String path) {
            Optional<Narrative> narrative = getClassLevelNarrativeFor(path);
            if (narrative.isPresent() && !isEmpty(narrative.get().type())) {
                return Optional.of(narrative.get().type());
            }
            return Optional.absent();
        }
    }

}
