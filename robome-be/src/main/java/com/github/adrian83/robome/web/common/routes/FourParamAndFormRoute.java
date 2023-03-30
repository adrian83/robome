package com.github.adrian83.robome.web.common.routes;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.function.Function;
import java.util.function.Supplier;

import com.github.adrian83.robome.common.function.PentaFunction;
import com.github.adrian83.robome.common.function.TetraFunction;

import akka.http.javadsl.server.Route;

public class FourParamAndFormRoute<T> extends AbsFormRoute<T> implements Supplier<Route> {

	private PentaFunction<String, String, String, String, Class<T>, Route> action;

	public FourParamAndFormRoute(String[] path, Class<T> clazz,
			PentaFunction<String, String, String, String, Class<T>, Route> action) {
		super(path, clazz);
		this.action = action;
	}

	public FourParamAndFormRoute(String path, Class<T> clazz,
			PentaFunction<String, String, String, String, Class<T>, Route> action) {
		super(path, clazz);
		this.action = action;
	}

	@Override
	public Route get() {

		if (emptyPath()) {
			throw new IllegalStateException("path should contains one parameter");
		}

		var newPath = pathTail();

		if (startsWithParameter()) {
			Function<String, TetraFunction<String, String, String, Class<T>, Route>> newFunc = (String var1) -> (
					String var2, String var3, String var4, Class<T> clz) -> action.apply(var1, var2, var3, var4, clz);
			return pathPrefix(segment(),
					var1 -> new ThreeParamsAndFormRoute<T>(newPath, getClazz(), newFunc.apply(var1)).get());
		}

		return pathPrefix(pathHead(), new FourParamAndFormRoute<T>(newPath, getClazz(), action));
	}
}
