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


package org.apache.isis.applib.internal.resources;

/**
 * 
 * package private helper class to store application scoped context-path (if any)
 *
 */
class _Resource_ContextPath extends _Resource_Path {

	public _Resource_ContextPath(String contextPath) {
		super(contextPath);
	}
	
	public String getContextPath() {
		return path;
	}

	@Override
	protected String resourceName() {
		return "context-path";
	}
	
}
