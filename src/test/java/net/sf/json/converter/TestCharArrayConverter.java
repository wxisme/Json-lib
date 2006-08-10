/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.json.converter;

import junit.framework.TestCase;
import net.sf.json.Assertions;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestCharArrayConverter extends TestCase
{
   public static void main( String[] args )
   {
      junit.textui.TestRunner.run( TestCharArrayConverter.class );
   }

   private CharArrayConverter converter;

   public TestCharArrayConverter( String name )
   {
      super( name );
   }

   public void testConvert_charArray()
   {
      char[] expected = { '1', '2', '3' };
      char[] actual = (char[]) converter.convert( expected );
      Assertions.assertEquals( expected, actual );
   }

   public void testConvert_charArray_threedims()
   {
      char[][][] expected = { { { '1' }, { '2' } }, { { '3' }, { '4' } } };
      char[][][] actual = (char[][][]) converter.convert( expected );
      Assertions.assertEquals( expected, actual );
   }

   public void testConvert_charArray_twodims()
   {
      char[][] expected = { { '1', '2', '3' }, { '4', '5', '6' } };
      char[][] actual = (char[][]) converter.convert( expected );
      Assertions.assertEquals( expected, actual );
   }

   public void testConvert_null()
   {
      assertNull( converter.convert( null ) );
   }

   public void testConvert_strings()
   {
      String[] expected = { "1", "2", "3.3" };
      char[] actual = (char[]) converter.convert( expected );
      Assertions.assertEquals( new char[] { '1', '2', '3' }, actual );
   }

   public void testConvert_strings_twodims()
   {
      String[][] expected = { { "1", "2", "3.3" }, { "4", "5", "6.6" } };
      char[][] actual = (char[][]) converter.convert( expected );
      Assertions.assertEquals( new char[][] { { '1', '2', '3' }, { '4', '5', '6' } }, actual );
   }

   protected void setUp() throws Exception
   {
      converter = new CharArrayConverter();
   }
}