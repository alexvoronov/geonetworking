/**
 * Upper Tester Application.
 *
 * See specification at http://webapp.etsi.org/workprogram/Report_WorkItem.asp?WKI_ID=42425
 *
 * Upper Tester Application is a part of System Under Test (SUT).
 *
 * Upper Tester Application triggers events in Interface Under Test (IUT).
 *
 * Upper Tester Application talks UDP to Test System (TS).
 *
 * <pre>
 *
 *   TS              SUT
 * +----+  UDP  +-----------+
 * |    |-------| UT -> IUT |
 * +----+       +-----------+
 *
 * </pre>
 *
 * The main class for Upper Tester Application is {@link net.gcdc.uppertester.ItsStation}.
 *
 */
package net.gcdc.uppertester;

