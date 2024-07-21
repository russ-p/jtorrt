package com.github.russp.jtorrt.util;

import com.github.russp.jtorrt.common.InfoHash;

public interface Tuple {

	static <A, B> Pair<A, B> of(A a, B b) {
		return new Pair<>(a, b);
	}

	static <A, B, C> Triplet<A, B, C> of(A a, B b, C c) {
		return new Triplet<>(a, b, c);
	}

	record Pair<A, B>(A a, B b) implements Tuple {
		public A left() {
			return a;
		}

		public B right() {
			return b;
		}

		public A key() {
			return a;
		}

		public B value() {
			return b;
		}

		public <C> Triplet<A, B, C> concat(C c) {
			return new Triplet<>(a(), b(), c);
		}
	}

	record Triplet<A, B, C>(A a, B b, C c) implements Tuple {

	}
}
