package com.voc.ide.plugin.env.psi;

import com.intellij.psi.tree.IElementType;
import com.voc.ide.plugin.env.EnvLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author WuYujie
 * @email coffee377@dingtalk.com
 * @time 2022/09/03 23:56
 */
public class EnvElementType extends IElementType {

    public EnvElementType(@NotNull @NonNls String debugName) {
        super(debugName, EnvLanguage.INSTANCE);
    }

}
