/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
File firstLog = new File( basedir, 'first.log' )
assert firstLog.exists()
var first = firstLog.text

// first run:
assert first.contains("basic-1.0.0.zip")
assert new File ( basedir, '../../it-repo/eu/maveniverse/maven/zippy/it/basic/1.0.0/basic-1.0.0.pom').exists()
assert new File ( basedir, '../../it-repo/eu/maveniverse/maven/zippy/it/basic/1.0.0/basic-1.0.0.zip').exists()
