/*
 * Copyright (c) 2007-2011 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
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

package cascading.tuple;

import java.io.IOException;

/** Interface TupleEntryCollector is used to allow {@link cascading.operation.BaseOperation} instances to emit result {@link Tuple} values. */
public abstract class TupleEntryCollector
  {
  protected TupleEntry tupleEntry = new TupleEntry( Fields.UNKNOWN, null );

  protected TupleEntryCollector()
    {
    }

  /**
   * Constructor TupleCollector creates a new TupleCollector instance.
   *
   * @param declared of type Fields
   */
  public TupleEntryCollector( Fields declared )
    {
    if( declared == null )
      throw new IllegalArgumentException( "declared fields must not be null" );

    if( declared.isUnknown() )
      return;

    this.tupleEntry = new TupleEntry( declared, Tuple.size( declared.size() ) );
    }

  /**
   * Method add inserts the given {@link TupleEntry} into the outgoing stream. Note the method {@link #add(Tuple)} is
   * more efficient as it simply calls {@link TupleEntry#getTuple()};
   *
   * @param tupleEntry of type TupleEntry
   */
  public void add( TupleEntry tupleEntry )
    {
    Fields expectedFields = this.tupleEntry.getFields();

    if( expectedFields.isUnknown() )
      this.tupleEntry.setTuple( tupleEntry.getTuple() );
    else
      this.tupleEntry.setTuple( tupleEntry.selectTuple( expectedFields ) );

    safeCollect( this.tupleEntry );
    }

  /**
   * Method add inserts the given {@link Tuple} into the outgoing stream.
   *
   * @param tuple of type Tuple
   */
  public void add( Tuple tuple )
    {
    if( !tupleEntry.getFields().isUnknown() && tupleEntry.getFields().size() != tuple.size() )
      throw new TupleException( "operation added the wrong number of fields, expected: " + tupleEntry.getFields().print() + ", got result size: " + tuple.size() );

    tupleEntry.setTuple( tuple );

    safeCollect( tupleEntry );
    }

  private void safeCollect( TupleEntry tupleEntry )
    {
    try
      {
      collect( tupleEntry );
      }
    catch( IOException exception )
      {
      throw new TupleException( "unable to collect tuple", exception );
      }
    }

  protected abstract void collect( TupleEntry tupleEntry ) throws IOException;

  /**
   * Method close closes the underlying resource being written to. This method should be called when no more {@link Tuple}
   * instances will be written out.
   */
  public void close()
    {
    // do nothing
    }

  }