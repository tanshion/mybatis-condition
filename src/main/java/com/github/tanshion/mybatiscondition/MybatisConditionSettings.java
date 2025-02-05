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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "MybatisConditionSettings",
        storages = @Storage("mybatis-condition.xml")
)
public class MybatisConditionSettings implements PersistentStateComponent<MybatisConditionSettings.State> {

    private State myState = new State();

    public static MybatisConditionSettings getInstance(Project project) {
        return project.getService(MybatisConditionSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public String getDbType() {
        return myState.dbType;
    }

    public void setDbType(String dbType) {
        myState.dbType = dbType;
    }

    public static class State {
        public String dbType = MybatisSqlGen.MYSQL; // 默认值
    }
}
