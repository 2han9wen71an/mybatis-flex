/*
 *  Copyright (c) 2022-2023, Mybatis-Flex (fuhai999@gmail.com).
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mybatisflex.core.query;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import java.util.List;
import java.util.Optional;

/**
 * {@link QueryWrapper} 链式调用。
 *
 * @author 王帅
 * @since 2023-07-22
 */
public class QueryChain<T> extends QueryWrapperAdapter<QueryChain<T>> implements MapperQueryChain<T> {

    private final BaseMapper<T> baseMapper;

    public QueryChain(BaseMapper<T> baseMapper) {
        this.baseMapper = baseMapper;
    }

    public static <E> QueryChain<E> of(BaseMapper<E> baseMapper) {
        return new QueryChain<>(baseMapper);
    }

    @Override
    public BaseMapper<T> baseMapper() {
        return baseMapper;
    }

    @Override
    public QueryWrapper toQueryWrapper() {
        return this;
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public T oneWithRelations() {
        return baseMapper.selectOneWithRelationsByQuery(this);
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public <R> R oneWithRelationsAs(Class<R> asType) {
        return baseMapper.selectOneWithRelationsByQueryAs(this, asType);
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public Optional<T> oneWithRelationsOpt() {
        return Optional.ofNullable(baseMapper.selectOneWithRelationsByQuery(this));
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public <R> Optional<R> oneWithRelationsAsOpt(Class<R> asType) {
        return Optional.ofNullable(baseMapper.selectOneWithRelationsByQueryAs(this, asType));
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public List<T> listWithRelations() {
        return baseMapper.selectListWithRelationsByQuery(this);
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public <R> List<R> listWithRelationsAs(Class<R> asType) {
        return baseMapper.selectListWithRelationsByQueryAs(this, asType);
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public Page<T> pageWithRelations(Page<T> page) {
        return baseMapper.paginateWithRelations(page, this);
    }

    /**
     * @deprecated 该方法将在 1.6.0 版本移除
     */
    @Deprecated
    public <R> Page<R> pageWithRelationsAs(Page<R> page, Class<R> asType) {
        return baseMapper.paginateWithRelationsAs(page, this, asType);
    }

    @Override
    public String toSQL() {
        TableInfo tableInfo = TableInfoFactory.ofMapperClass(baseMapper.getClass());
        CPI.setFromIfNecessary(this, tableInfo.getSchema(), tableInfo.getTableName());
        return super.toSQL();
    }

}
