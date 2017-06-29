package parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiangyuan5 on 2017/6/29.
 */
public class StringParse {
    private String str;
    private static final String patternStr = "(reqtime:\\d+)|(available_pos:-?\\d+)|(feedsnum:-?\\d+)|(unread_status:-?\\d+)|(service_name:.*?\\u001c)";

    public  StringParse(String s) {
        str = s;
    }

    public Result parsing() {

        //String s = "reqtime:1478761203\u001Creqid:14787612037225447291279814\u001Cuid:5447291279\u001Cfrom:106B095010\u001Cplatform:android\u001Cversion:6.B.0\u001Cservice_name:main_feed\u001Cip:10.237.145.13\u001Cproxy_source:4269835110\u001Cwm:3333_1001\u001Cavailable_pos:0\u001Ccategory_r:sc|idx\u001Cis_unread_pool:0\u001Cproduct_r:Aim|TopFans|AddFans|Brand|FanstopExtend|Bidfeed|Apploft|WAX\u001Clast_span:-1\u001Ctmeta_l2:channel:self_service\u001Dposition:1\u001Dvalue:200\u001Dcategory:idx\u001Dproduct:TopFans\u001Dtmeta_l3:itemid:4040000469218356\u001Floc:1\u001Fmark:1_B24941E95D531C4E371C1E213EFD252BF139A60D2A2B4C8407D8D6739BA11092FB4BB1A8BB8A6F75817FC0331DA7A675281F2BF7CC648EF90325E3BDEFC6A93C52CD71D575BA95B4FE73A4843C5C81C02BF63FA9C4D1F6824D8B5D826F2D34964BC920333D5439F0EA05FEA73EC25281\u001Dtype:011020\u001Cunread_status:-1\u001Cfeedsnum:1\u001Cloadmore:0";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(str);

        Result parseRes = new Result();
        while(matcher.find()) {
            String[] item = matcher.group().split(":");
            switch (item[0]) {
                case "service_name" :
                    parseRes.setServiceName(item[1].substring(0, item[1].length()-1) );
                    break;
                case "available_pos" :
                    parseRes.setAvailPos(Integer.parseInt(item[1]));
                    break;
                case "unread_status" :
                    parseRes.setUnreadStatus(Integer.parseInt(item[1]));
                    break;
                case "feedsnum" :
                    parseRes.setFeedsNum(Integer.parseInt(item[1]));
                    break;
                case "reqtime":
                    parseRes.setReqtime(Long.parseLong(item[1]));
                    break;
                default:
                    break;
            }

        }

        parseRes.setDateTime();

        return parseRes;
    }


}
