package matteo.utils;

public class SubmodelVariables {

    public static final String model = "[seq(a1,a2),xor_split(a2,[a3,a5]),seq(a3,a4),seq(a5,a6),xor_split(a6,[a7,a8]),and_split(a8,[a9,a10])," +
            "and_join([a9,a10],a11),seq(a11,a12),xor_join([a4, a7, a12], a13),seq(a13,a14)]";
    public static final String lastEvent = "end_of_the_world";
    public static final String observability = "[obs(start,never),obs(a1,always),obs(a2,always),obs(a3,always),obs(a4,always),obs(a5,always)," +
            "obs(a6,always),obs(a7,always),obs(a8,always),obs(a9,always),obs(a10,always),obs(a11,always),obs(a12,always),obs(a13,always),obs(a14,always),obs(stop,never)]";
    public static final String durationConstr = "[(a1,(5,10)),(a2,(5,10)),(a3,(30,40))," +
            "(a4,(150,200)),(a5,(20,40)),(a6,(5,10)),(a7,(200,225)),(a8,(10,20)),(a9,(30,40)),(a10,(80,80)),(a11,(100,120)),(a12,(200,400)),(a13,(15,20)),(a14,(5,10))]";
    public static final String interTaskConstr = "[(a1, '_start', a2, '_end', 1, [0,30] ), " +
            "(a8, '_start', a11, '_start', 2, [100,140] ), (a10, '_end', a11, '_start', 1, [20,10000] ), (a10, '_start', a11, '_end', 1, [0,250] )]";

    //public static final String options  = "[trace_max_length(43)]"
    public static final String options = "[ trace_max_length(43), generate_current_time(no), activities_have_start_and_end, double_chained_translation ]";


    public static final String[] submodels = new String[]{
            "[ seq(a1, a2), xor_split(a2,[ a3, a5 ]), seq(a3, a4) ]",     //0
            "[ seq(a5, a6), xor_split(a6,[ a7, a8 ]) ]",                                      //1
            "[ and_split(a8, [ a9, a10 ] ), and_join( [ a9, a10 ], a11 ) , seq(a11, a12) ]",  //2
            "[ xor_join([a4, a7, a12], a13), seq(a13, a14) ]"
    };                            //3

    public static final String[] observabilities = new String[]{
            "[obs(start,never),obs(a1,always),obs(a2,always),obs(a3,always),obs(a4,always),obs(a5,always)]",
            "[obs(a5,always),obs(a6,always),obs(a7,always),obs(a8,always)]",
            "[obs(a8,always),obs(a9,always),obs(a10,always),obs(a11,always),obs(a12,always)]",
            "[obs(a4,always),obs(a7,always),obs(a12,always),obs(a13,always),obs(a14,always),obs(stop,never)]"
    };

    public static final String[] durationConstrs = new String[]{
            "[ (a1, (5,10)), (a2, (5,10)), (a3, (30,40)) ]",
            "[ (a5, (20,40)), (a6, (5,10)), (a8, (10,20))]",
            "[ (a9, (30,40)), (a10, (80,80)) , (a11, (100,120)) ]",
            "[ (a4, (150,200)), (a7, (200,225)), (a12, (200,400)), (a13, (15,20)), (a14, (5,10)) ]"};

    public static final String[] interTaskConstrs = new String[]{
            "[ (a1, '_start', a2, '_end', 1, [0,30] ) ]",
            "[]",
            "[ (a8, '_start', a11, '_start', 2, [100,140] ), (a10, '_end', a11, '_start', 1, [20,10000] ), (a10, '_start', a11, '_end', 1, [0,250] )]",
            "[]"
    };


}
