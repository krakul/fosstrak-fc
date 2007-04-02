package org.accada.ale.client;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.accada.ale.util.SerializerUtil;
import org.accada.ale.xsd.ale.epcglobal.ECReports;

import util.ECElementsUtils;
import junit.framework.TestCase;

public class ReportHandlerTest extends TestCase implements ReportHandlerListener {

	private static final int NOTIFICATION_PORT = 6789;

	private static final ECReports EC_REPORTS = ECElementsUtils.createECReports();
	
	private ReportHandler reportHandler;
	private ECReports receivedReports = null;

	protected void setUp() throws Exception {
		
		super.setUp();
		reportHandler = new ReportHandler(NOTIFICATION_PORT);

	}
	
	protected void tearDown() throws Exception {
		
		super.tearDown();
		reportHandler.stop();
		
	}
	
	public void testSubscribe() throws Exception {
		
		reportHandler.addListener(this);
		
	}
	
	public void testUnsubscribe() throws Exception {
		
		reportHandler.removeListener(this);
		
	}
	
	public void testNotifyListener() throws Exception {
		
		reportHandler.addListener(this);
		writeDataToLocalhotPort(EC_REPORTS, NOTIFICATION_PORT);
		for(int i = 0; i < 100; i++) {
			Thread.sleep(10);
		}
		assertNotNull(receivedReports);
		ECElementsUtils.assertEquals(EC_REPORTS, receivedReports);

	}

	private void writeDataToLocalhotPort(ECReports reports, int port) throws IOException {

		CharArrayWriter writer = new CharArrayWriter();
		SerializerUtil.serializeECReports(reports, writer);
		
		Socket socket = new Socket("localhost", port);
		OutputStream out = socket.getOutputStream();
		out.write(writer.toString().getBytes());
		out.write("\n".getBytes());
		out.flush();
		
	}

	public void dataReceived(ECReports reports) {
		
		receivedReports = reports;
		
	}
	
}