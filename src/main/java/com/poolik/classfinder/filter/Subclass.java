/*---------------------------------------------------------------------------*\
  $Id$
  ---------------------------------------------------------------------------
  This software is released under a BSD-style license:

  Copyright (c) 2004-2007 Brian M. Clapper. All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are
  met:

  1.  Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

  2.  The end-user documentation included with the redistribution, if any,
      must include the following acknowlegement:

        "This product includes software developed by Brian M. Clapper
        (bmc@clapper.org, http://www.clapper.org/bmc/). That software is
        copyright (c) 2004-2007 Brian M. Clapper."

      Alternately, this acknowlegement may appear in the software itself,
      if wherever such third-party acknowlegements normally appear.

  3.  Neither the names "clapper.org", "clapper.org Java Utility Library",
      nor any of the names of the project contributors may be used to
      endorse or promote products derived from this software without prior
      written permission. For written permission, please contact
      bmc@clapper.org.

  4.  Products derived from this software may not be called "clapper.org
      Java Utility Library", nor may "clapper.org" appear in their names
      without prior written permission of Brian M. Clapper.

  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
  NO EVENT SHALL BRIAN M. CLAPPER BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\*---------------------------------------------------------------------------*/

package com.poolik.classfinder.filter;

import com.poolik.classfinder.ClassHierarchyResolver;
import com.poolik.classfinder.info.ClassInfo;

import java.util.Map;

/**
 * <p><tt>Subclass</tt> is a {@link ClassFilter} that matches
 * class names that (a) can be loaded and (b) extend a given subclass or
 * implement a specified interface, directly or indirectly. It uses the
 * <tt>java.lang.Class.isAssignableFrom()</tt> method, so it actually has to
 * load each class it tests. For maximum flexibility, a
 * <tt>Subclass</tt> can be configured to use a specific class
 * loader.</p>
 *
 * @author Copyright &copy; 2006 Brian M. Clapper
 * @version <tt>$Revision$</tt>
 */
public class Subclass implements ClassFilter {
  private final Class baseClass;

  public static Subclass of(Class baseClassOrInterface) {
    return new Subclass(baseClassOrInterface);
  }

  public Subclass(Class baseClassOrInterface) {
    this.baseClass = baseClassOrInterface;
  }

  /**
   * Perform the acceptance test on the loaded <tt>Class</tt> object.
   *
   * @return <tt>true</tt> if the class name matches,
   * <tt>false</tt> if it doesn't
   */
  public boolean accept(ClassInfo classInfo, ClassHierarchyResolver hierarchyResolver) {
    Map<String, ClassInfo> superClasses;
    if (baseClass.isInterface())
      superClasses = hierarchyResolver.findAllInterfaces(classInfo);
    else
      superClasses = hierarchyResolver.findAllSuperClasses(classInfo);

    return superClasses.keySet().contains(baseClass.getName());
  }
}