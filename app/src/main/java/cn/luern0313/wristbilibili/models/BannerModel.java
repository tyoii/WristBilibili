package cn.luern0313.wristbilibili.models;

import cn.luern0313.lson.annotation.field.LsonPath;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BannerModel
{
    @LsonPath("current_time")
    private long currentTime;

    @LsonPath("banner_end_time")
    private long bannerEndTime;

    @LsonPath("text")
    private String text;

    @LsonPath("image_urls")
    private String[] imageUrls;

    @LsonPath("jump_url")
    private String jumpUrl;
}
