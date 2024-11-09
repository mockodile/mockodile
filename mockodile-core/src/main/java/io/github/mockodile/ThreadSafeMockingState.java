package io.github.mockodile;

class ThreadSafeMockingState {

    private static final ThreadLocal<MockingState> MOCKING_STATE = ThreadLocal.withInitial(MockingState::new);

    private ThreadSafeMockingState() {
    }

    static MockingState mockingState() {
        return MOCKING_STATE.get();
    }

    static void clear() {
        MOCKING_STATE.remove();
    }
}
