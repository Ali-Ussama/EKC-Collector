package util;


import java.util.HashMap;

/**
 * Created By Ali Ussama 28/10/2019
 */
public class OConstants {

    public static final String LAYER_DISTRIBUTION_BOX = "EKC_Collector.DBO.FCL_DISTRIBUTIONBOX";

    public static final String LAYER_RMU = "EKC_Collector.DBO.FCL_RMU";

    public static final String LAYER_POLES = "EKC_Collector.DBO.FCL_POLES";

    public static final String LAYER_SUB_STATION = "EKC_Collector.DBO.FCL_Substation";

    public static final String LAYER_OCL_METER = "EKC_Collector.DBO.OCL_METER";

    public static final String LAYER_SERVICE_POINT = "EKC_Collector.DBO.Service_Point";

    public static HashMap<String, String> dist_box_domain, fcl_poles_domain, fcl_rmu_domain, fcl_substation_domain, ocl_meter_domain, service_point_domain;

    public static HashMap<String, String> getDistBoxDomain() {

        dist_box_domain = new HashMap<>();
        dist_box_domain.put("0", "النوع");
        dist_box_domain.put("MB", "Mini_Pillar");
        dist_box_domain.put("EXB", "EXTENSIBLE");
        dist_box_domain.put("MC", "MCCB");

        return dist_box_domain;
    }

    public static HashMap<String, String> getFCLPOLESDomain() {

        fcl_poles_domain = new HashMap<>();
        fcl_poles_domain.put("0", "النوع");
        fcl_poles_domain.put("LV", "عمود ضغط منخفض");
        fcl_poles_domain.put("MV", "عمود ضغط متوسط");
        fcl_poles_domain.put("ST", "انارة شوارع");

        return fcl_poles_domain;
    }

    public static HashMap<String, String> getFCLRMUDomain() {

        fcl_rmu_domain = new HashMap<>();
        fcl_rmu_domain.put("0", "النوع");
        fcl_rmu_domain.put("RMU", "RMU");

        return fcl_rmu_domain;
    }

    public static HashMap<String, String> getFCLSubStationDomain() {

        fcl_substation_domain = new HashMap<>();
        fcl_substation_domain.put("0", "النوع");
        fcl_substation_domain.put("PL", "محول هوائي");
        fcl_substation_domain.put("PKG", "محطة مغلقة تحتوى على معدات (LVDB-SG-TS)");
        fcl_substation_domain.put("UNT", "محطة خارجية تحتوى على (LVDB-SG-TS)");
        fcl_substation_domain.put("SW", "Switch");

        return fcl_substation_domain;
    }

    public static HashMap<String, String> getOCLMeterDomain() {

        ocl_meter_domain = new HashMap<>();
        ocl_meter_domain.put("0", "النوع");
        ocl_meter_domain.put("HC", "Meter");

        return ocl_meter_domain;
    }

    public static HashMap<String, String> getServicePointDomain() {

        service_point_domain = new HashMap<>();
        service_point_domain.put("0", "النوع");
        service_point_domain.put("SB", "SERVICE BOX");
        return service_point_domain;
    }

    public static HashMap<String, String> getDomain(String layerName) {
        switch (layerName) {
            case LAYER_DISTRIBUTION_BOX:
                return getDistBoxDomain();
            case LAYER_POLES:
                return getFCLPOLESDomain();
            case LAYER_RMU:
                return getFCLRMUDomain();
            case LAYER_SUB_STATION:
                return getFCLSubStationDomain();
            case LAYER_OCL_METER:
                return getOCLMeterDomain();
            default:
                return getServicePointDomain();
        }
    }
}
