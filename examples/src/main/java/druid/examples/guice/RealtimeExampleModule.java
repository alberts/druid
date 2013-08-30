/*
 * Druid - a distributed column store.
 * Copyright (C) 2012, 2013  Metamarkets Group Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package druid.examples.guice;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.metamx.common.logger.Logger;
import com.metamx.druid.loading.DataSegmentPusher;
import com.metamx.druid.realtime.FireDepartment;
import com.metamx.druid.realtime.RealtimeManager;
import com.metamx.druid.realtime.SegmentPublisher;
import druid.examples.flights.FlightsFirehoseFactory;
import druid.examples.rand.RandomFirehoseFactory;
import druid.examples.twitter.TwitterSpritzerFirehoseFactory;
import druid.examples.web.WebFirehoseFactory;
import io.druid.client.DataSegment;
import io.druid.client.DruidServer;
import io.druid.client.InventoryView;
import io.druid.client.ServerView;
import io.druid.guice.guice.FireDepartmentsProvider;
import io.druid.guice.guice.JsonConfigProvider;
import io.druid.guice.guice.ManageLifecycle;
import io.druid.guice.guice.NoopSegmentPublisherProvider;
import io.druid.guice.guice.RealtimeManagerConfig;
import io.druid.initialization.DruidModule;
import io.druid.server.coordination.DataSegmentAnnouncer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/**
 */
public class RealtimeExampleModule implements DruidModule
{
  private static final Logger log = new Logger(RealtimeExampleModule.class);

  @Override
  public void configure(Binder binder)
  {
    binder.bind(SegmentPublisher.class).toProvider(NoopSegmentPublisherProvider.class);
    binder.bind(DataSegmentPusher.class).to(NoopDataSegmentPusher.class);
    binder.bind(DataSegmentAnnouncer.class).to(NoopDataSegmentAnnouncer.class);
    binder.bind(InventoryView.class).to(NoopInventoryView.class);
    binder.bind(ServerView.class).to(NoopServerView.class);

    JsonConfigProvider.bind(binder, "druid.realtime", RealtimeManagerConfig.class);
    binder.bind(
        new TypeLiteral<List<FireDepartment>>()
        {
        }
    ).toProvider(FireDepartmentsProvider.class);
    binder.bind(RealtimeManager.class).in(ManageLifecycle.class);
  }

  @Override
  public List<com.fasterxml.jackson.databind.Module> getJacksonModules()
  {
    return Arrays.<com.fasterxml.jackson.databind.Module>asList(
        new SimpleModule("RealtimeExampleModule")
            .registerSubtypes(
                new NamedType(TwitterSpritzerFirehoseFactory.class, "twitzer"),
                new NamedType(FlightsFirehoseFactory.class, "flights"),
                new NamedType(RandomFirehoseFactory.class, "rand"),
                new NamedType(WebFirehoseFactory.class, "webstream")
            )
    );
  }

  private static class NoopServerView implements ServerView
  {
    @Override
    public void registerServerCallback(
        Executor exec, ServerCallback callback
    )
    {
      // do nothing
    }

    @Override
    public void registerSegmentCallback(
        Executor exec, SegmentCallback callback
    )
    {
      // do nothing
    }
  }

  private static class NoopInventoryView implements InventoryView
  {
    @Override
    public DruidServer getInventoryValue(String string)
    {
      return null;
    }

    @Override
    public Iterable<DruidServer> getInventory()
    {
      return ImmutableList.of();
    }
  }

  private static class NoopDataSegmentPusher implements DataSegmentPusher
  {
    @Override
    public DataSegment push(File file, DataSegment segment) throws IOException
    {
      return segment;
    }
  }

  private static class NoopDataSegmentAnnouncer implements DataSegmentAnnouncer
  {
    @Override
    public void announceSegment(DataSegment segment) throws IOException
    {
      // do nothing
    }

    @Override
    public void unannounceSegment(DataSegment segment) throws IOException
    {
      // do nothing
    }

    @Override
    public void announceSegments(Iterable<DataSegment> segments) throws IOException
    {
      // do nothing
    }

    @Override
    public void unannounceSegments(Iterable<DataSegment> segments) throws IOException
    {
      // do nothing
    }
  }
}