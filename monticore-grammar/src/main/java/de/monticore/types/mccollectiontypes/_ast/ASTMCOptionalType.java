package de.monticore.types.mccollectiontypes._ast;

import com.google.common.collect.Lists;

import java.util.List;

public class ASTMCOptionalType extends ASTMCOptionalTypeTOP {

    public ASTMCOptionalType() {
    }

    public ASTMCOptionalType(String name, ASTMCTypeArgument typeArgument) {
        super(name, typeArgument);
    }

    @Override
    public List<String> getNameList() {
        return Lists.newArrayList(getName());
    }
    @Override
    public String getBaseName() {
        return getName();
    }
    @Override
    public void setNameList(List<String> names) {
        setName(names.get(0));
    }

    @Override
    public List<ASTMCTypeArgument> getMCTypeArgumentList() {
        return Lists.newArrayList(getMCTypeArgument());
    }

    @Override
    public void setMCTypeArgumentList(List<ASTMCTypeArgument> arguments) {
        setMCTypeArgument(arguments.get(0));
    }

}
