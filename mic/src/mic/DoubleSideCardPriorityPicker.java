package mic;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by TerTer on 2017-03-10.
 */
public class DoubleSideCardPriorityPicker {

    private static Map<String, MasterUpgradeData.UpgradeGrants> doubleSideCardAdditionalData = ImmutableMap.<String, MasterUpgradeData.UpgradeGrants>builder()
            .put("pivotwing", new MasterUpgradeData.UpgradeGrants() {
                @Override
                public String getName() {
                    return "agility";
                }

                @Override
                public String getType() {
                    return "stats";
                }

                @Override
                public int getValue() {
                    return 1;
                }

                @Override
                public boolean isStatsModifier() {
                    return true;
                }
            })
            .put("adaptability", new MasterUpgradeData.UpgradeGrants() {
                @Override
                public String getName() {
                    return "skill";
                }

                @Override
                public String getType() {
                    return "stats";
                }

                @Override
                public int getValue() {
                    return 1;
                }

                @Override
                public boolean isStatsModifier() {
                    return true;
                }
            })
            .build();

    public static MasterUpgradeData.UpgradeGrants getDoubleSideCardStats(String name) {
        MasterUpgradeData.UpgradeGrants upgradeGrants = doubleSideCardAdditionalData.get(name);
        if (upgradeGrants != null) {
            if ("pivotwing".equals(name)) {
                UserInformer.setInformUserAboutUWingPivotWing();
            } else if ("adaptability".equals(name)) {
                UserInformer.setInformUserAboutAdaptability();
            }
        }
        return upgradeGrants;
    }

}
