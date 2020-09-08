package ua.sushi_master.test;

//         Case status_id
//        1	Passed
//        2	Blocked
//        3	Untested (not allowed when adding a result)
//        4	Retest
//        5	Failed
public class TestRailConst {
    private  TestRailConst(){

    }
    //status_id
    public final static String STATUS_PASSED = "1";
    public final static String STATUS_BLOCKED = "2";
    public final static String STATUS_UNTESTED = "3";
    public final static String STATUS_RETEST = "4";
    public final static String STATUS_FAILED = "5";

    public final static String TEST_TYPE_AUTOMATED = "2";

}
