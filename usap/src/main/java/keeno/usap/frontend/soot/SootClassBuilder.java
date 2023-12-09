

package keeno.usap.frontend.soot;

import keeno.usap.language.annotation.AnnotationHolder;
import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JClassBuilder;
import keeno.usap.language.classes.JField;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.classes.Modifier;
import keeno.usap.language.generics.ClassGSignature;
import keeno.usap.language.generics.GSignatures;
import keeno.usap.language.type.ClassType;
import keeno.usap.util.collection.Lists;
import soot.SootClass;
import soot.tagkit.SignatureTag;
import soot.tagkit.Tag;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

import static keeno.usap.language.classes.ClassNames.OBJECT;

class SootClassBuilder implements JClassBuilder {

    private final Converter converter;

    private final SootClass sootClass;

    SootClassBuilder(Converter converter, SootClass sootClass) {
        this.converter = converter;
        this.sootClass = sootClass;
    }

    @Override
    public void build(JClass jclass) {
        jclass.build(this);
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Modifiers.convert(sootClass.getModifiers());
    }

    @Override
    public String getSimpleName() {
        return sootClass.getShortName();
    }

    @Override
    public ClassType getClassType() {
        return (ClassType) converter.convertType(sootClass.getType());
    }

    @Override
    public JClass getSuperClass() {
        if (sootClass.getName().equals(OBJECT)) {
            return null;
        } else {
            return converter.convertClass(sootClass.getSuperclass());
        }
    }

    @Override
    public Collection<JClass> getInterfaces() {
        return Lists.map(sootClass.getInterfaces(), converter::convertClass);
    }

    @Override
    public JClass getOuterClass() {
        return sootClass.hasOuterClass() ?
                converter.convertClass(sootClass.getOuterClass()) :
                null;
    }

    @Override
    public Collection<JField> getDeclaredFields() {
        return Lists.map(sootClass.getFields(), converter::convertField);
    }

    @Override
    public Collection<JMethod> getDeclaredMethods() {
        return Lists.map(sootClass.getMethods(), converter::convertMethod);
    }

    @Override
    public AnnotationHolder getAnnotationHolder() {
        return Converter.convertAnnotations(sootClass);
    }

    @Override
    public boolean isApplication() {
        return sootClass.isApplicationClass();
    }

    @Override
    public boolean isPhantom() {
        return sootClass.isPhantom();
    }

    @Nullable
    @Override
    public ClassGSignature getGSignature() {
        Tag tag = sootClass.getTag("SignatureTag");
        if (tag instanceof SignatureTag signatureTag) {
            return GSignatures.toClassSig(sootClass.isInterface(),
                    signatureTag.getSignature());
        }
        return null;
    }
}
