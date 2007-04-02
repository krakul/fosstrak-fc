/*
 * Copyright (c) 2006, ETH Zurich
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of the ETH Zurich nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.accada.ale.server;

import java.net.URL;

import junit.framework.TestCase;

import org.accada.ale.server.EventCycle;
import org.accada.ale.server.Report;
import org.accada.ale.server.ReportsGenerator;
import org.accada.ale.util.HexUtil;
import org.accada.ale.wsdl.ale.epcglobal.ECSpecValidationException;
import org.accada.ale.wsdl.ale.epcglobal.ImplementationException;
import org.accada.ale.xsd.ale.epcglobal.ECBoundarySpec;
import org.accada.ale.xsd.ale.epcglobal.ECFilterSpec;
import org.accada.ale.xsd.ale.epcglobal.ECReport;
import org.accada.ale.xsd.ale.epcglobal.ECReportGroup;
import org.accada.ale.xsd.ale.epcglobal.ECReportGroupListMember;
import org.accada.ale.xsd.ale.epcglobal.ECReportOutputSpec;
import org.accada.ale.xsd.ale.epcglobal.ECReportSetEnum;
import org.accada.ale.xsd.ale.epcglobal.ECReportSetSpec;
import org.accada.ale.xsd.ale.epcglobal.ECReportSpec;
import org.accada.ale.xsd.ale.epcglobal.ECSpec;
import org.accada.ale.xsd.ale.epcglobal.ECTime;
import org.accada.ale.xsd.ale.epcglobal.ECTimeUnit;
import org.accada.reader.msg.notification.TagType;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author regli
 */
public class ReportTest extends TestCase {

	// default report spec parameters
	private static final String REPORT_NAME = "TestReport";
	private static final boolean REPORT_ONLY_ON_CHANGE = false;
	private static final boolean REPORT_IF_EMPTY = true;
	
	// default set spec parameters
	private static final ECReportSetEnum SET_SPEC = ECReportSetEnum.CURRENT;
	
	// default filter spec parameters
	private static final String[] INCLUDE_PATTERNS = new String[] {"urn:epc:pat:gid-96:1.*.*"};
	private static final String[] EXCLUDE_PATTERNS = new String[] {"urn:epc:pat:gid-96:5.*.*"};
	
	// default group spec parameters
	private static final String[] GROUP_PATTERNS = new String[] {"urn:epc:pat:gid-96:1.[0-10].X"};
	
	// default output sepc parameters
	private static final boolean INCLUDE_COUNT = true;
	private static final boolean INCLUDE_EPC = true;
	private static final boolean INCLUDE_RAW_DECIMAL = true;
	private static final boolean INCLUDE_RAW_HEX = true;
	private static final boolean INCLUDE_TAG = true;
	
	// default tag parameters
	private static final byte[] TAG_ID = new byte[] {1, 2, 3};
	private static final String TAG_PURE_URI = "urn:epc:pat:gid-96:1.2.3";
	private static final String TAG_TAG_URI = "urn:epc:pat:gid-96:1.2.3";
	
	// default group tag parameters
	private static final byte[] DEFAULT_GROUP_TAG_ID = new byte[] {1, 11, 3};
	private static final String DEFAULT_GROUP_TAG_PURE_URI = "urn:epc:pat:gid-96:1.11.3";
	private static final String DEFAULT_GROUP_TAG_TAG_URI = "urn:epc:pat:gid-96:1.11.3";
	
	// last event cycle default tag parameters
	private static final byte[] LAST_CYCLE_TAG_ID = new byte[] {1, 2, 3};
	private static final String LAST_CYCLE_TAG_PURE_URI = "urn:epc:pat:gid-96:1.3.3";
	private static final String LAST_CYCLE_TAG_TAG_URI = "urn:epc:pat:gid-96:1.3.3";
	
	// last event cycle default group tag parameters
	private static final byte[] LAST_CYCLE_DEFAULT_GROUP_TAG_ID = new byte[] {1, 11, 3};
	private static final String LAST_CYCLE_DEFAULT_GROUP_TAG_PURE_URI = "urn:epc:pat:gid-96:1.12.3";
	private static final String LAST_CYCLE_DEFAULT_GROUP_TAG_TAG_URI = "urn:epc:pat:gid-96:1.12.3";
	
	// not included tag parameters
	private static final byte[] EXCLUDED_TAG_ID = new byte[] {5, 2, 3};
	private static final String EXCLUDED_TAG_PURE_URI = "urn:epc:pat:gid-96:5.2.3";
	private static final String EXCLUDED_TAG_TAG_URI = "urn:epc:pat:gid-96:5.2.3";
	
	private static final String GROUP_NAME = "urn:epc:pat:gid-96:1.[0-10].3";
	
	private ECReportSpec reportSpec;
	private EventCycle eventCycle;
	
	protected void setUp() throws Exception {
		
		super.setUp();
		
		// configure Logger with properties file
		URL url = this.getClass().getResource("/log4j.properties");
		PropertyConfigurator.configure(url);
		
		// create ReportSpec
		reportSpec = createECReportSpec();
		
		// create EventCycle
		eventCycle = createEventCycle();
		
	}
	
	public void test_DefaultReportSpec() throws Exception {
		
		// create report
		Report report = new Report(reportSpec, eventCycle);
		
		// add event
		report.addTag(createTag(TAG_ID, TAG_PURE_URI, TAG_TAG_URI));
		report.addTag(createTag(DEFAULT_GROUP_TAG_ID, DEFAULT_GROUP_TAG_PURE_URI, DEFAULT_GROUP_TAG_TAG_URI));
		report.addTag(createTag(EXCLUDED_TAG_ID, EXCLUDED_TAG_PURE_URI, EXCLUDED_TAG_TAG_URI));
		
		// get ECReport
		ECReport ecReport = report.getECReport();

		// test report name
		assertEquals(REPORT_NAME, ecReport.getReportName());

		// test groups
		ECReportGroup[] groups = ecReport.getGroup();
		assertEquals(2, groups.length);
		
		// test group
		for (ECReportGroup group : groups) {
			if (group.getGroupName() == null) {
				
				// test default group
				assertEquals(null, group.getGroupName());
				assertEquals(1, group.getGroupCount().getCount());
				
				// test members
				ECReportGroupListMember[] members = group.getGroupList().getMember();
				assertEquals(1, members.length);
				
				// test member
				ECReportGroupListMember member = members[0];
				assertEquals(DEFAULT_GROUP_TAG_PURE_URI, member.getRawDecimal().get_value());
				assertEquals(HexUtil.byteArrayToHexString(DEFAULT_GROUP_TAG_ID), member.getRawHex().get_value());
				assertEquals(DEFAULT_GROUP_TAG_TAG_URI, member.getTag().get_value());
				// TODO: test epc
				//assertEquals("unkown", member.getEpc());
				
			} else {
				
				// test group 'urn:epc:pat:gid-96:1.[0-10].3'
				assertEquals(GROUP_NAME, group.getGroupName());
				assertEquals(1, group.getGroupCount().getCount());
				
				// test members
				ECReportGroupListMember[] members = group.getGroupList().getMember();
				assertEquals(1, members.length);
				
				// test member
				ECReportGroupListMember member = members[0];
				assertEquals(TAG_PURE_URI, member.getRawDecimal().get_value());
				assertEquals(HexUtil.byteArrayToHexString(TAG_ID), member.getRawHex().get_value());
				assertEquals(TAG_TAG_URI, member.getTag().get_value());
				// TODO: test epc
				// assertEquals("unkown", member.getEpc());
				
			}
		}
		
	}
	
	public void test_AdditionsReportSpec() throws Exception {
		
		// modify spec
		reportSpec.getReportSet().setSet(ECReportSetEnum.ADDITIONS);
		
		// create report
		Report report = new Report(reportSpec, eventCycle);
		
		// add event
		report.addTag(createTag(TAG_ID, TAG_PURE_URI, TAG_TAG_URI));
		report.addTag(createTag(DEFAULT_GROUP_TAG_ID, DEFAULT_GROUP_TAG_PURE_URI, DEFAULT_GROUP_TAG_TAG_URI));
		report.addTag(createTag(EXCLUDED_TAG_ID, EXCLUDED_TAG_PURE_URI, EXCLUDED_TAG_TAG_URI));
		
		// get ECReport
		ECReport ecReport = report.getECReport();

		// test report name
		assertEquals(REPORT_NAME, ecReport.getReportName());

		// test groups
		ECReportGroup[] groups = ecReport.getGroup();
		assertEquals(2, groups.length);
		
		// test group
		for (ECReportGroup group : groups) {
			if (group.getGroupName() == null) {
				
				// test default group
				assertEquals(null, group.getGroupName());
				assertEquals(1, group.getGroupCount().getCount());
				
				// test members
				ECReportGroupListMember[] members = group.getGroupList().getMember();
				assertEquals(1, members.length);
				
				// test member
				ECReportGroupListMember member = members[0];
				assertEquals(DEFAULT_GROUP_TAG_PURE_URI, member.getRawDecimal().get_value());
				assertEquals(HexUtil.byteArrayToHexString(DEFAULT_GROUP_TAG_ID), member.getRawHex().get_value());
				assertEquals(DEFAULT_GROUP_TAG_TAG_URI, member.getTag().get_value());
				// TODO: test epc
				//assertEquals("unkown", member.getEpc());
				
			} else {
				
				// test group 'urn:epc:pat:gid-96:1.[0-10].3'
				assertEquals(GROUP_NAME, group.getGroupName());
				assertEquals(1, group.getGroupCount().getCount());
				
				// test members
				ECReportGroupListMember[] members = group.getGroupList().getMember();
				assertEquals(1, members.length);
				
				// test member
				ECReportGroupListMember member = members[0];
				assertEquals(TAG_PURE_URI, member.getRawDecimal().get_value());
				assertEquals(HexUtil.byteArrayToHexString(TAG_ID), member.getRawHex().get_value());
				assertEquals(TAG_TAG_URI, member.getTag().get_value());
				// TODO: test epc
				// assertEquals("unkown", member.getEpc());
				
			}
		}
		
	}
	
	public void test_AdditionsReportWithoutAdditionSpec() throws Exception {
		
		// modify spec
		reportSpec.getReportSet().setSet(ECReportSetEnum.ADDITIONS);
		
		// create report
		Report report = new Report(reportSpec, eventCycle);
		
		// add event
		report.addTag(createTag(LAST_CYCLE_TAG_ID, LAST_CYCLE_TAG_PURE_URI, LAST_CYCLE_TAG_TAG_URI));
		report.addTag(createTag(LAST_CYCLE_DEFAULT_GROUP_TAG_ID, LAST_CYCLE_DEFAULT_GROUP_TAG_PURE_URI,
				LAST_CYCLE_DEFAULT_GROUP_TAG_TAG_URI));
		report.addTag(createTag(EXCLUDED_TAG_ID, EXCLUDED_TAG_PURE_URI, EXCLUDED_TAG_TAG_URI));
		
		// get ECReport
		ECReport ecReport = report.getECReport();

		// test report name
		assertEquals(REPORT_NAME, ecReport.getReportName());

		// test groups
		assertNull(ecReport.getGroup());
		
	}
	
	public void test_DeletionsReportSpec() throws Exception {
		
		// modify spec
		reportSpec.getReportSet().setSet(ECReportSetEnum.DELETIONS);
		
		// create report
		Report report = new Report(reportSpec, eventCycle);
		
		// add event
		report.addTag(createTag(TAG_ID, TAG_PURE_URI, TAG_TAG_URI));
		report.addTag(createTag(DEFAULT_GROUP_TAG_ID, DEFAULT_GROUP_TAG_PURE_URI, DEFAULT_GROUP_TAG_TAG_URI));
		report.addTag(createTag(EXCLUDED_TAG_ID, EXCLUDED_TAG_PURE_URI, EXCLUDED_TAG_TAG_URI));
		
		// get ECReport
		ECReport ecReport = report.getECReport();

		// test report name
		assertEquals(REPORT_NAME, ecReport.getReportName());

		// test groups
		ECReportGroup[] groups = ecReport.getGroup();
		assertEquals(2, groups.length);
		
		// test group
		for (ECReportGroup group : groups) {
			if (group.getGroupName() == null) {
				
				// test default group
				assertEquals(null, group.getGroupName());
				assertEquals(1, group.getGroupCount().getCount());
				
				// test members
				ECReportGroupListMember[] members = group.getGroupList().getMember();
				assertEquals(1, members.length);
				
				// test member
				ECReportGroupListMember member = members[0];
				assertEquals(LAST_CYCLE_DEFAULT_GROUP_TAG_PURE_URI, member.getRawDecimal().get_value());
				assertEquals(HexUtil.byteArrayToHexString(LAST_CYCLE_DEFAULT_GROUP_TAG_ID), member.getRawHex().get_value());
				assertEquals(LAST_CYCLE_DEFAULT_GROUP_TAG_TAG_URI, member.getTag().get_value());
				// TODO: test epc
				//assertEquals("unkown", member.getEpc());
				
			} else {
				
				// test group 'urn:epc:pat:gid-96:1.[0-10].3'
				assertEquals(GROUP_NAME, group.getGroupName());
				assertEquals(1, group.getGroupCount().getCount());
				
				// test members
				ECReportGroupListMember[] members = group.getGroupList().getMember();
				assertEquals(1, members.length);
				
				// test member
				ECReportGroupListMember member = members[0];
				assertEquals(LAST_CYCLE_TAG_PURE_URI, member.getRawDecimal().get_value());
				assertEquals(HexUtil.byteArrayToHexString(LAST_CYCLE_TAG_ID), member.getRawHex().get_value());
				assertEquals(LAST_CYCLE_TAG_TAG_URI, member.getTag().get_value());
				// TODO: test epc
				// assertEquals("unkown", member.getEpc());
				
			}
		}
		
	}
	
	public void test_DeletionsReportSpecWithoutDeletions() throws Exception {
		
		// modify spec
		reportSpec.getReportSet().setSet(ECReportSetEnum.DELETIONS);
		
		// create report
		Report report = new Report(reportSpec, eventCycle);
		
		// add event
		eventCycle.addTag(createTag(LAST_CYCLE_TAG_ID, LAST_CYCLE_TAG_PURE_URI, LAST_CYCLE_TAG_TAG_URI));
		eventCycle.addTag(createTag(LAST_CYCLE_DEFAULT_GROUP_TAG_ID, LAST_CYCLE_DEFAULT_GROUP_TAG_PURE_URI,
				LAST_CYCLE_DEFAULT_GROUP_TAG_TAG_URI));
		
		// get ECReport
		ECReport ecReport = report.getECReport();

		// test report name
		assertEquals(REPORT_NAME, ecReport.getReportName());

		// test groups
		assertNull(ecReport.getGroup());
		
	}
	
	private TagType createTag(byte[] tag_id, String tag_pure_uri, String tag_tag_uri) {
		
		TagType tag = new TagType();
		tag.setTagID(tag_id);
		tag.setTagIDAsPureURI(tag_pure_uri);
		tag.setTagIDAsTagURI(tag_tag_uri);
		
		return tag;
		
	}

	private ECReportSpec createECReportSpec() {
		
		// create spec
		ECReportSpec spec = new ECReportSpec();
		
		// set paramteters
		spec.setReportName(REPORT_NAME);
		spec.setReportOnlyOnChange(REPORT_ONLY_ON_CHANGE);
		spec.setReportIfEmpty(REPORT_IF_EMPTY);
		spec.setReportSet(createECReportSetSpec());
		spec.setFilterSpec(createECFilterSpec());
		spec.setGroupSpec(GROUP_PATTERNS);
		spec.setOutput(createECReportOutputSpec());
		
		return spec;
		
	}

	private ECReportSetSpec createECReportSetSpec() {
		
		// create setSpec
		ECReportSetSpec setSpec = new ECReportSetSpec();
		
		// set parameters
		setSpec.setSet(SET_SPEC);
		
		return setSpec; 
		
	}
	
	private ECFilterSpec createECFilterSpec() {
		
		// create spec
		ECFilterSpec spec = new ECFilterSpec();
		
		// set include patterns
		spec.setIncludePatterns(INCLUDE_PATTERNS);
		
		// set exclude patterns
		spec.setExcludePatterns(EXCLUDE_PATTERNS);
		
		return spec;
		
	}
	
	private ECReportOutputSpec createECReportOutputSpec() {
		
		// create spec
		ECReportOutputSpec spec = new ECReportOutputSpec();
		
		// set parameters
		spec.setIncludeCount(INCLUDE_COUNT);
		spec.setIncludeEPC(INCLUDE_EPC);
		spec.setIncludeRawDecimal(INCLUDE_RAW_DECIMAL);
		spec.setIncludeRawHex(INCLUDE_RAW_HEX);
		spec.setIncludeTag(INCLUDE_TAG);
		
		return spec;
		
	}
	
	private EventCycle createEventCycle() throws ImplementationException, ECSpecValidationException {
	
		// create EventCycle
		EventCycle eventCycle = new EventCycle(createReportsGenerator(), createLastEventCycle());
		
		return eventCycle;
		
	}
	
	private EventCycle createLastEventCycle() throws ImplementationException, ECSpecValidationException {
		
		// create EventCycle
		EventCycle eventCycle = new EventCycle(createReportsGenerator(), null);
		eventCycle.addTag(createTag(LAST_CYCLE_TAG_ID, LAST_CYCLE_TAG_PURE_URI, LAST_CYCLE_TAG_TAG_URI));
		eventCycle.addTag(createTag(LAST_CYCLE_DEFAULT_GROUP_TAG_ID, LAST_CYCLE_DEFAULT_GROUP_TAG_PURE_URI,
				LAST_CYCLE_DEFAULT_GROUP_TAG_TAG_URI));
		eventCycle.addTag(createTag(EXCLUDED_TAG_ID, EXCLUDED_TAG_PURE_URI, EXCLUDED_TAG_TAG_URI));
		eventCycle.stop();
		
		return eventCycle;
		
	}
	
	private ReportsGenerator createReportsGenerator() throws ECSpecValidationException, ImplementationException {
		
		// create ReportsGenerator
		ReportsGenerator reportsGenerator = new ReportsGenerator(REPORT_NAME, createECSpec());
		
		return reportsGenerator;
		
	}
	
	private ECSpec createECSpec() {
		
		// create ECSpec
		ECSpec ecSpec = new ECSpec();
		
		// set parameter
		ecSpec.setBoundarySpec(createECBoundarySpec());
		ecSpec.setIncludeSpecInReports(false);
		ecSpec.setLogicalReaders(null);
		ecSpec.setReportSpecs(createECReportSpecs());
		
		return ecSpec;
		
	}
	
	private ECReportSpec[] createECReportSpecs() {
		
		// create ECReportSpecs
		ECReportSpec[] ecReportSpecs = new ECReportSpec[1];
		
		// add report spec
		ecReportSpecs[0] = reportSpec;
		
		return ecReportSpecs;
		
	}
	
	private ECBoundarySpec createECBoundarySpec() {
		
		// create spec
		ECBoundarySpec spec = new ECBoundarySpec();

		// set duration
		spec.setDuration(getECTimeInMS(0));
		
		// set repeat period
		spec.setRepeatPeriod(getECTimeInMS(0));
		
		// set stabel set interval
		spec.setStableSetInterval(getECTimeInMS(0));
		
		// set start trigger
		spec.setStartTrigger(null);
		
		// set stop trigger
		spec.setStopTrigger(null);
		
		return spec;
		
	}
	
	private ECTime getECTimeInMS(long value) {
		
		ECTime time = new ECTime();
		time.setUnit(ECTimeUnit.MS);
		time.set_value(value);
		
		return time;
		
	}
	
}