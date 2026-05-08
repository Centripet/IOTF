package batisGenerator;


import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:postgresql://localhost:45434/iotf?currentSchema=main",
                        "root",
                        "W2316195243"
                )
                .globalConfig(builder -> {
                    builder.author("Centripet")
                            .outputDir(System.getProperty("user.dir") + "/common/src/main/java/batisGenerator")
                            .disableOpenDir();
                })
                .packageConfig(builder -> {
                    builder.parent("batisGenerator")
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    System.getProperty("user.dir") + "/common/src/main/resources/mapper"));
                })
                .strategyConfig(builder -> {
                    builder.addInclude("t_operation_log")
                            .addTablePrefix("tbl_")
                            .entityBuilder()
                            // 表名 → 转驼峰生成类名
                            .naming(NamingStrategy.underline_to_camel)
                            // 字段名 → 保持和数据库一致
                            .columnNaming(NamingStrategy.no_change)
                            .enableColumnConstant(); // 添加注解保留字段名
                })
                .execute();
    }
}