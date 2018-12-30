package nl.knokko.doubleserver.handler;

public class TestSocketHandlerFactory implements WebSocketHandlerFactory {

	@Override
	public WebSocketHandler createHandler() {
		return new TestSocketHandler();
	}
}