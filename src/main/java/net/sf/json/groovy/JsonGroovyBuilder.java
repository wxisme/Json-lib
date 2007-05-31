/*
 * Copyright 2002-2007 the original author or authors.
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

package net.sf.json.groovy;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JsonGroovyBuilder extends GroovyObjectSupport {
   private static final String JSON = "json";
   private Map properties;
   private Stack stack;
   private JSON current;

   public JsonGroovyBuilder() {
      stack = new Stack();
      properties = new HashMap();
   }

   public Object getProperty( String name ) {
      if( !stack.isEmpty() ){
         Object top = stack.peek();
         if( top instanceof JSONObject ){
            JSONObject json = (JSONObject) top;
            if( json.containsKey( name ) ){
               return json.get( name );
            }else{
               return _getProperty( name );
            }
         }else{
            return _getProperty( name );
         }
      }else{
         return _getProperty( name );
      }
   }

   public Object invokeMethod( String name, Object arg ) {
      if( JSON.equals( name ) && stack.isEmpty() ){
         return createObject( name, arg );
      }

      Object[] args = (Object[]) arg;
      if( args.length == 0 ){
         throw new MissingMethodException( name, getClass(), args );
      }

      JSONObject object = new JSONObject();

      if( args.length > 1 ){
         stack.push( new JSONArray() );
      }
      for( int i = 0; i < args.length; i++ ){
         if( args[i] instanceof Closure ){
            append( name, createObject( (Closure) args[i] ) );
         }else if( args[i] instanceof Map ){
            append( name, createObject( (Map) args[i] ) );
         }else if( args[i] instanceof List ){
            append( name, createArray( (List) args[i] ) );
         }
      }
      if( args.length > 1 ){
         current = (JSON) stack.pop();
      }

      object.element( name, current );
      current = object;

      return current;
   }

   public void setProperty( String name, Object value ) {
      if( value instanceof GString ){
         value = value.toString();
         try{
            value = JSONSerializer.toJSON( value );
         }catch( JSONException jsone ){
            // its a String literal
         }
      }else if( value instanceof Closure ){
         value = createObject( (Closure) value );
      }else if( value instanceof Map ){
         value = createObject( (Map) value );
      }else if( value instanceof List ){
         value = createArray( (List) value );
      }

      append( name, value );
   }

   private Object _getProperty( String name ) {
      if( properties.containsKey( name ) ){
         return properties.get( name );
      }else{
         return super.getProperty( name );
      }
   }

   private void append( String key, Object value ) {
      Object target = null;
      if( !stack.isEmpty() ){
         target = stack.peek();
         current = (JSON)target;
         if( target instanceof JSONObject ){
            ((JSONObject) target).accumulate( key, value );
         }else if( target instanceof JSONArray ){
            ((JSONArray) target).element( value );
         }
      }else{
         properties.put( key, value );
      }
   }

   private JSONArray createArray( List list ) {
      JSONArray array = new JSONArray();
      stack.push( array );
      for( Iterator elements = list.iterator(); elements.hasNext(); ){
         Object element = elements.next();
         if( element instanceof Closure ){
            element = createObject( (Closure) element );
         }else if( element instanceof Map ){
            element = createObject( (Map) element );
         }else if( element instanceof List ){
            element = createArray( (List) element );
         }
         array.element( element );
      }
      stack.pop();
      return array;
   }

   private JSONObject createObject( Closure closure ) {
      JSONObject object = new JSONObject();
      stack.push( object );
      closure.setDelegate( this );
      closure.call();
      stack.pop();
      return object;
   }

   private JSONObject createObject( Map map ) {
      JSONObject object = new JSONObject();
      stack.push( object );
      for( Iterator properties = map.entrySet()
            .iterator(); properties.hasNext(); ){
         Map.Entry property = (Map.Entry) properties.next();
         String key = String.valueOf( property.getKey() );
         Object value = property.getValue();
         if( value instanceof Closure ){
            value = createObject( (Closure) value );
         }else if( value instanceof Map ){
            value = createObject( (Map) value );
         }else if( value instanceof List ){
            value = createArray( (List) value );
         }
         object.element( key, value );
      }
      stack.pop();
      return object;
   }

   private Object createObject( String name, Object arg ) {
      Object[] args = (Object[]) arg;
      if( args.length == 0 ){
         throw new MissingMethodException( name, getClass(), args );
      }

      if( args.length == 1 ){
         if( args[0] instanceof Closure ){
            return createObject( (Closure) args[0] );
         }else if( args[0] instanceof Map ){
            return createObject( (Map) args[0] );
         }else if( args[0] instanceof List ){
            return createArray( (List) args[0] );
         }else{
            throw new JSONException( "!!!" );
         }
      }else{
         JSONArray array = new JSONArray();
         stack.push( array );
         for( int i = 0; i < args.length; i++ ){
            if( args[i] instanceof Closure ){
               append( name, createObject( (Closure) args[i] ) );
            }else if( args[i] instanceof Map ){
               append( name, createObject( (Map) args[i] ) );
            }else if( args[i] instanceof List ){
               append( name, createArray( (List) args[i] ) );
            }else{
               throw new JSONException( "!!!" );
            }
         }
         stack.pop();
         return array;
      }
   }
}