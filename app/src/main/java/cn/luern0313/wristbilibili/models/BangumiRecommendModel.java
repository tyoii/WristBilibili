package cn.luern0313.wristbilibili.models;

import java.util.ArrayList;

import cn.luern0313.lson.annotation.field.LsonPath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BangumiRecommendModel
{
    @LsonPath("list[*]")
    private ArrayList<ListBangumiModel> listBangumiModelArrayList;
}
