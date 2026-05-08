package org.iotf.entity.auth.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 文件
 * </p>
 *
 * @author Centripet
 * @since 2026-05-08
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_file")
public class TFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "file_id", type = IdType.ASSIGN_ID)
    private Long file_id;

    private String file_key;

    private Boolean is_public_read;

    private Long uploader;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    private Integer type;

    private String suffix;

    private String title;

    private String info;

    private String origin_name;

    private Long file_size;

    @TableField(exist = false)
    private String oss_url;

    @TableField(exist = false)
    private String preview_url;

    public static final String FILE_ID = "file_id";

    public static final String FILE_KEY = "file_key";

    public static final String IS_PUBLIC_READ = "is_public_read";

    public static final String UPLOADER = "uploader";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String TYPE = "type";

    public static final String SUFFIX = "suffix";

    public static final String TITLE = "title";

    public static final String INFO = "info";

    public static final String ORIGIN_NAME = "origin_name";

    public static final String FILE_SIZE = "file_size";

    @Override
    public String toString() {
        return "TFile{" +
        "file_id = " + file_id +
        ", file_key = " + file_key +
        ", is_public_read = " + is_public_read +
        ", uploader = " + uploader +
        ", create_time = " + create_time +
        ", update_time = " + update_time +
        ", type = " + type +
        ", suffix = " + suffix +
        ", title = " + title +
        ", info = " + info +
        ", origin_name = " + origin_name +
        ", file_size = " + file_size +
        "}";
    }
}
