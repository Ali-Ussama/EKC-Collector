package util;


import java.util.ArrayList;

/**
 * Created By Ali Ussama 28/10/2019
 */
public class OConstants {

    public static final String LAYER_DISTRIBUTION_BOX = "Distribution box";

    public static final String LAYER_URM = "Distribution box";

    public static final String LAYER_POLES = "Poles";

    public static final String LAYER_SUB_STATION = "Sub Station";

    public static final String LAYER_OCL_METER = "OCL Meter";

    public static final String LAYER_SERVICE_POINT = "Service Point";

    public static ArrayList<String> dist_box_domain_keys, dist_box_domain_values;

    static {
        dist_box_domain_keys = new ArrayList<String>();
        dist_box_domain_keys.add("");
        dist_box_domain_keys.add("");
        dist_box_domain_keys.add("");
        dist_box_domain_keys.add("");
        dist_box_domain_keys.add("");

        dist_box_domain_values = new ArrayList<>();
        dist_box_domain_values.add("");
        dist_box_domain_values.add("");
        dist_box_domain_values.add("");
        dist_box_domain_values.add("");
        dist_box_domain_values.add("");


    }


}
