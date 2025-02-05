/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tanshion.mybatiscondition;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

public class OpenMybatisSqlGenDialogAction extends AnAction {

    public static Project project;

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();
        // 获取当前编辑器
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            System.out.println("No editor found.");
            return;
        }

        // 获取当前文件
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            System.out.println("No PSI file found.");
            return;
        }

        // 获取光标位置
        int offset = editor.getCaretModel().getOffset();
        // 获取光标位置的PsiElement
        PsiElement elementAtCaret = psiFile.findElementAt(offset);

        // 尝试从PsiTypeElement获取PsiClass
        PsiTypeElement typeElement = PsiTreeUtil.getParentOfType(elementAtCaret, PsiTypeElement.class);
        if (typeElement != null) {
            PsiType psiType = typeElement.getType();
            if (psiType instanceof PsiClassType classType) {
                PsiClass psiClass = classType.resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiParameter获取PsiClass
        PsiParameter parameter = PsiTreeUtil.getParentOfType(elementAtCaret, PsiParameter.class);
        if (parameter != null) {
            PsiType parameterType = parameter.getType();
            if (parameterType instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) parameterType).resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiField获取PsiClass
        PsiField field = PsiTreeUtil.getParentOfType(elementAtCaret, PsiField.class);
        if (field != null) {
            PsiType fieldType = field.getType();
            if (fieldType instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) fieldType).resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiLocalVariable获取PsiClass
        PsiLocalVariable localVariable = PsiTreeUtil.getParentOfType(elementAtCaret, PsiLocalVariable.class);
        if (localVariable != null) {
            PsiType variableType = localVariable.getType();
            if (variableType instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) variableType).resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiMethod返回类型获取PsiClass
        PsiMethod method = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethod.class);
        if (method != null) {
            PsiType returnType = method.getReturnType();
            if (returnType instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) returnType).resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiReferenceExpression获取PsiClass
        PsiReferenceExpression referenceExpression = PsiTreeUtil.getParentOfType(elementAtCaret, PsiReferenceExpression.class);
        if (referenceExpression != null) {
            PsiElement target = referenceExpression.resolve();
            if (target instanceof PsiClass psiClass) {
                MybatisSqlGenDialog.showDialog(project, psiClass);
                return;
            }
        }

        // 尝试从PsiNewExpression获取PsiClass
        PsiNewExpression newExpression = PsiTreeUtil.getParentOfType(elementAtCaret, PsiNewExpression.class);
        if (newExpression != null) {
            PsiType newType = newExpression.getType();
            if (newType instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) newType).resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiClassObjectAccessExpression获取PsiClass
        PsiClassObjectAccessExpression classObjectAccessExpression = PsiTreeUtil.getParentOfType(elementAtCaret, PsiClassObjectAccessExpression.class);
        if (classObjectAccessExpression != null) {
            PsiType classType = classObjectAccessExpression.getOperand().getType();
            if (classType instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) classType).resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiInstanceOfExpression获取PsiClass
        PsiInstanceOfExpression instanceOfExpression = PsiTreeUtil.getParentOfType(elementAtCaret, PsiInstanceOfExpression.class);
        if (instanceOfExpression != null) {
            PsiTypeElement checkTypeElement = instanceOfExpression.getCheckType();
            if (checkTypeElement != null) {
                PsiType checkType = checkTypeElement.getType();
                if (checkType instanceof PsiClassType) {
                    PsiClass psiClass = ((PsiClassType) checkType).resolve();
                    if (psiClass != null) {
                        MybatisSqlGenDialog.showDialog(project, psiClass);
                        return;
                    }
                }
            }
        }

        // 尝试从PsiThrowStatement获取PsiClass
        PsiThrowStatement throwStatement = PsiTreeUtil.getParentOfType(elementAtCaret, PsiThrowStatement.class);
        if (throwStatement != null) {
            PsiExpression exceptionExpression = throwStatement.getException();
            if (exceptionExpression instanceof PsiNewExpression) {
                PsiType exceptionType = exceptionExpression.getType();
                if (exceptionType instanceof PsiClassType) {
                    PsiClass psiClass = ((PsiClassType) exceptionType).resolve();
                    if (psiClass != null) {
                        MybatisSqlGenDialog.showDialog(project, psiClass);
                        return;
                    }
                }
            }
        }

        // 尝试从PsiCatchSection获取PsiClass
        PsiCatchSection catchSection = PsiTreeUtil.getParentOfType(elementAtCaret, PsiCatchSection.class);
        if (catchSection != null) {
            PsiParameter parameterInCatch = catchSection.getParameter();
            if (parameterInCatch != null) {
                PsiType parameterType = parameterInCatch.getType();
                if (parameterType instanceof PsiClassType) {
                    PsiClass psiClass = ((PsiClassType) parameterType).resolve();
                    if (psiClass != null) {
                        MybatisSqlGenDialog.showDialog(project, psiClass);
                        return;
                    }
                }
            }
        }

        // 尝试从PsiAnnotation获取PsiClass
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(elementAtCaret, PsiAnnotation.class);
        if (annotation != null) {
            PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
            if (referenceElement != null) {
                PsiElement target = referenceElement.resolve();
                if (target instanceof PsiClass psiClass) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiLambdaExpression获取PsiClass
        PsiLambdaExpression lambdaExpression = PsiTreeUtil.getParentOfType(elementAtCaret, PsiLambdaExpression.class);
        if (lambdaExpression != null) {
            PsiType functionalInterfaceType = lambdaExpression.getFunctionalInterfaceType();
            if (functionalInterfaceType instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) functionalInterfaceType).resolve();
                if (psiClass != null) {
                    MybatisSqlGenDialog.showDialog(project, psiClass);
                    return;
                }
            }
        }

        // 尝试从PsiMethodCallExpression获取PsiClass
        PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(elementAtCaret, PsiMethodCallExpression.class);
        if (methodCallExpression != null) {
            PsiMethod methodResolved = methodCallExpression.resolveMethod();
            if (methodResolved != null) {
                PsiClass containingClass = methodResolved.getContainingClass();
                if (containingClass != null) {
                    MybatisSqlGenDialog.showDialog(project, containingClass);
                    return;
                }
            }
        }


        System.out.println("Could not find a suitable PsiClass.");
    }
}
