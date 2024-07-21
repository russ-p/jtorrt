package com.github.russp.jtorrt.app;

import com.github.russp.jtorrt.UpdateService;
import com.github.russp.jtorrt.common.Storage;
import com.github.russp.jtorrt.http.ClientFacade;
import com.github.russp.jtorrt.http.TorrentsFacade;
import com.github.russp.jtorrt.http.TrackerFacade;
import com.github.russp.jtorrt.metrics.RpcMetricsService;
import com.github.russp.jtorrt.rpc.ClientService;
import com.github.russp.jtorrt.tracker.TrackerService;

import java.util.function.Function;

public record JTorrtAppInstance(
		Storage storage,
		ClientService clientService,
		TrackerService trackerService,
		UpdateService updateService,
		RpcMetricsService rpcMetricsService,
		ClientFacade clientFacade,
		TorrentsFacade torrentsFacade,
		TrackerFacade trackerFacade) implements JTorrtApp {

	public static JTorrtAppInstance.Builder builder() {
		return new JTorrtAppInstance.Builder();
	}

	public static class Builder implements JTorrtApp {

		private final ComponentRegistry<JTorrtApp> registry = new ComponentRegistry<>();

		public JTorrtAppInstance build() {
			return new JTorrtAppInstance(
					storage(),
					clientService(),
					trackerService(),
					updateService(),
					rpcMetricsService(),
					clientFacade(),
					torrentsFacade(),
					trackerFacade()
			);
		}

		@Override
		public Storage storage() {
			return registry.get(Storage.class).apply(this);
		}

		public Builder storage(Function<JTorrtApp, Storage> provider) {
			registry.singleton(Storage.class, provider);
			return this;
		}

		@Override
		public ClientService clientService() {
			return registry.get(ClientService.class).apply(this);
		}

		public Builder clientService(Function<JTorrtApp, ClientService> provider) {
			registry.singleton(ClientService.class, provider);
			return this;
		}

		@Override
		public TrackerService trackerService() {
			return registry.get(TrackerService.class).apply(this);
		}

		public Builder trackerService(Function<JTorrtApp, TrackerService> provider) {
			registry.singleton(TrackerService.class, provider);
			return this;
		}

		@Override
		public UpdateService updateService() {
			return registry.get(UpdateService.class).apply(this);
		}

		@Override
		public RpcMetricsService rpcMetricsService() {
			return registry.get(RpcMetricsService.class).apply(this);
		}

		@Override
		public ClientFacade clientFacade() {
			return registry.get(ClientFacade.class).apply(this);
		}

		@Override
		public TorrentsFacade torrentsFacade() {
			return registry.get(TorrentsFacade.class).apply(this);
		}

		@Override
		public TrackerFacade trackerFacade() {
			return registry.get(TrackerFacade.class).apply(this);
		}

		public <T> Builder add(Class<T> clazz, Function<JTorrtApp, T> provider) {
			registry.singleton(clazz, provider);
			return this;
		}
	}
}
