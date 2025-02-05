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

import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.github.tanshion.mybatiscondition.OpenMybatisSqlGenDialogAction.project;

public class MybatisSqlGen {

    public static final String MYSQL = "mysql";
    public static final String ORACLE = "oracle";
    public static final String POSTGRESQL = "postgresql";


    public static final String IN = "IN";
    public static final String NOT_IN = "NOT IN";
    public static final String BETWEEN = "BETWEEN";
    public static final String NOT_BETWEEN = "NOT BETWEEN";
    public static final String IS_NULL = "IS NULL";
    public static final String NOT_NULL = "NOT NULL";
    public static final String LIKE = "LIKE";
    public static final String EQ = "=";
    public static final String NOT_EQ = "!=";
    public static final String GT = ">";
    public static final String LT = "<";
    public static final String GTE = ">=";
    public static final String LTE = "<=";

    public static final Map<String, String> operationMap = Map.ofEntries(
            Map.entry(IN, "IN"),
            Map.entry(NOT_IN, "NOT IN"),
            Map.entry(BETWEEN, "BETWEEN"),
            Map.entry(NOT_BETWEEN, "NOT BETWEEN"),
            Map.entry(IS_NULL, "IS NULL"),
            Map.entry(NOT_NULL, "NOT NULL"),
            Map.entry(LIKE, "LIKE"),
            Map.entry(EQ, "="),
            Map.entry(NOT_EQ, "!="),
            Map.entry(GT, "&gt;"),
            Map.entry(LT, "&lt;"),
            Map.entry(GTE, "&gt;="),
            Map.entry(LTE, "&lt;=")
    );


    public static String generateMyBatisConditions(PsiClass psiClass, String tableAlias, String paramPrefix, Map<String, String> fieldOperation, String dbType) {
        StringBuilder conditions = new StringBuilder();
        for (PsiField field : psiClass.getAllFields()) {
            String fieldName = field.getName();
            String underlineFieldName = StrUtil.toUnderlineCase(fieldName);
            String fullParamName = (StrUtil.isBlank(paramPrefix) ? "" : paramPrefix + ".") + fieldName;
            String fullTableName = (StrUtil.isBlank(tableAlias) ? "" : tableAlias + ".") + underlineFieldName;

            String operationKey = fieldOperation.get(fieldName);
            operationKey = StrUtil.isBlank(operationKey) ? EQ : operationKey;
            String operation = operationMap.get(operationKey);
            if (field.getType().getCanonicalText().equals(CommonClassNames.JAVA_LANG_STRING)) {
                //对应 like eq
                if (LIKE.equals(operationKey)) {
                    appendLikeCondition(conditions, fullParamName, fullTableName, operation, dbType);
                } else {
                    appendEqCondition(conditions, fullParamName, fullTableName);
                }
            } else if (isListOfType(field, LocalDateTime.class, Date.class)) {
                //对应 between
                appendBetweenCondition(conditions, fullParamName, fullTableName, operation);
            } else if (isList(field)) {
                //对应 in notIn between
                if (IN.equals(operationKey) || NOT_IN.equals(operationKey)) {
                    appendInCondition(conditions, fullParamName, fullTableName, operation);
                } else if (BETWEEN.equals(operationKey) || NOT_BETWEEN.equals(operationKey)) {
                    appendBetweenCondition(conditions, fullParamName, fullTableName, operation);
                }
            } else if (field.getType().getCanonicalText().equals(CommonClassNames.JAVA_LANG_BOOLEAN) || field.getType().getCanonicalText().equals("boolean")) {
                //对应 boolean
                appendEqCondition(conditions, fullParamName, fullTableName);
            } else {
                //对应 eq notEq gt lt gte lte
                appendGeneralCondition(conditions, fullParamName, fullTableName, operation);
            }
        }

        return conditions.toString();
    }

    public static boolean isList(PsiField field) {
        // 获取字段类型
        PsiType fieldType = field.getType();

        // 获取字段类型的类
        PsiClass fieldClass = PsiUtil.resolveClassInType(fieldType);

        if (fieldClass == null) {
            return false;
        }
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiClass listClass = psiFacade.findClass(CommonClassNames.JAVA_UTIL_COLLECTION, GlobalSearchScope.allScope(project));
        if (listClass == null) {
            return false;
        }
        // 检查字段类型是否为集合类型
        return fieldClass.isInheritor(listClass, true);
    }

    public static boolean isListOfType(PsiField field, Class<?>... elementType) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiClass listClass = psiFacade.findClass(CommonClassNames.JAVA_UTIL_COLLECTION, GlobalSearchScope.allScope(project));
        if (listClass == null) {
            return false;
        }
        // 获取字段类型
        PsiType fieldType = field.getType();

        // 检查字段类型是否为 List
        if (!(fieldType instanceof PsiClassType classType)) {
            return false;
        }

        PsiClass fieldClass = classType.resolve();

        if (fieldClass == null || !fieldClass.isInheritor(listClass, true)) {
            return false;
        }

        // 获取泛型参数
        PsiType[] parameters = classType.getParameters();
        if (parameters.length != 1) {
            return false;
        }

        // 获取泛型参数的类
        PsiClass parameterClass = PsiUtil.resolveClassInType(parameters[0]);
        if (parameterClass == null) {
            return false;
        }

        // 检查泛型参数是否匹配
        for (Class<?> type : elementType) {
            if (Objects.equals(parameterClass.getQualifiedName(), type.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }


    private static void appendLikeCondition(StringBuilder conditions, String fullParamName, String fullTableName, String operation, String dbType) {
        if (ORACLE.equals(dbType)) {
            conditions.append(String.format("""
                    <if test="%s != null and %s != ''">
                        AND %s LIKE CONCAT(CONCAT('%%',#{%s}),'%%')
                    </if>
                    """, fullParamName, fullParamName, fullTableName, fullParamName));
        } else {
            conditions.append(String.format("""
                    <if test="%s != null and %s != ''">
                        AND %s LIKE CONCAT('%%', #{%s}, '%%')
                    </if>
                    """, fullParamName, fullParamName, fullTableName, fullParamName));
        }

    }

    private static void appendEqCondition(StringBuilder conditions, String fullParamName, String fullTableName) {
        conditions.append(String.format("""
                <if test="%s != null and %s != ''">
                    AND %s = #{%s}
                </if>
                """, fullParamName, fullParamName, fullTableName, fullParamName));
    }

    private static void appendBetweenCondition(StringBuilder conditions, String fullParamName, String fullTableName, String operation) {
        conditions.append(String.format("""
                <if test="%s != null and %s.size() == 2">
                    AND %s %s #{%s[0]} AND #{%s[1]}
                </if>
                """, fullParamName, fullParamName, fullTableName, operation, fullParamName, fullParamName));
    }

    private static void appendInCondition(StringBuilder conditions, String fullParamName, String fullTableName, String operation) {
        conditions.append(String.format("""
                <if test="%s != null and %s.size() > 0">
                    AND %s %s
                    <foreach item="item" index="index" collection="%s" open="(" separator="," close=")">
                        #{item}
                    </foreach>
                </if>
                """, fullParamName, fullParamName, fullTableName, operation, fullParamName));
    }

    private static void appendGeneralCondition(StringBuilder conditions, String fullParamName, String fullTableName, String operation) {
        conditions.append(String.format("""
                <if test="%s != null">
                    AND %s %s #{%s}
                </if>
                """, fullParamName, fullTableName, operation, fullParamName));
    }
}
