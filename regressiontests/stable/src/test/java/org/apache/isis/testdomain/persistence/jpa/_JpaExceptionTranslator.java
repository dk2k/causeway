/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testdomain.persistence.jpa;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.jpa.JpaTransactionManager;

import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.exceptions._Exceptions;

final class _JpaExceptionTranslator {

    //TODO[2502] if only we found a way to have this code run at a lower level already 
    static DataAccessException translate(Throwable failure, JpaTransactionManager txManager) {
        
        return (DataAccessException) Result.failure(failure)
        
        .mapFailure(ex-> _Exceptions.streamCausalChain(ex)
                .filter(e->e instanceof RuntimeException)
                .map(RuntimeException.class::cast)
                // call Spring's exception translation mechanism
                .map(nextEx->DataAccessUtils.translateIfNecessary(nextEx, txManager.getJpaDialect()))
                .filter(nextEx -> nextEx instanceof DataAccessException)
                .findFirst()
                .orElseGet(()->new RuntimeException(ex)))

      //TODO[2502] do similar to JDO here, so we have a better understanding what's going on step by step        
//        // asserts we have a NucleusException
//        .ifFailure(ex->assertTrue(ex instanceof NucleusException))
//        
//        // converts to JDOException
//        .mapFailure(ex->ex instanceof NucleusException
//                ? NucleusJDOHelper
//                        .getJDOExceptionForNucleusException(((NucleusException)ex))
//                : ex)
//        
//        // asserts translation to JDO standard
//        .ifFailure(ex->assertTrue(ex instanceof JDODataStoreException))
//        
//        // converts to Spring DataAccessException
//        .mapFailure(ex->ex instanceof JDOException
//                ? txManager.getJdoDialect().translateException((JDOException)ex)
//                : ex)
        
        .getFailure()
        .orElseThrow();

    }
    
     

}
