package com.voc.ide.plugin.style.css;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.inspections.suppress.CssInspectionSuppressor;
import com.voc.ide.plugin.style.INumberUnitInspection;
import org.jetbrains.annotations.NotNull;

/**
 * @author Wu Yujie
 * @email coffee377@dingtalk.com
 * @time 2021/09/23 17:22
 */
public class CssInspection extends CssInspectionSuppressor implements INumberUnitInspection {

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        if (this.withRpxOrUpxUnit(element, toolId)) {
            return true;
        }
        return super.isSuppressedFor(element, toolId);
    }

}
