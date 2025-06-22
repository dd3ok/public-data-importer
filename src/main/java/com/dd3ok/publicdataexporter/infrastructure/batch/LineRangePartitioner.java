package com.dd3ok.publicdataexporter.infrastructure.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// 메서드에서 파일의 총 라인 수를 계산하고, gridSize(CPU 코어 수)로 나누어
// 각 파티션이 처리할 start, endLine 정보를 생성합니다.
@Slf4j
public class LineRangePartitioner implements Partitioner {

    private Resource resource;

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long totalLines = 0;
        try {
            // 파일의 전체 라인 수를 계산합니다.
            totalLines = countLines(resource) - 1; // 헤더 제외
        } catch (Exception e) {
            log.error("Failed to count lines in resource: {}", resource, e);
            throw new RuntimeException("Failed to count lines", e);
        }

        Assert.isTrue(totalLines > 0, "File must not be empty.");
        
        long linesPerPartition = totalLines / gridSize;
        log.info("Total lines: {}, Partitions: {}, Lines per partition: {}", totalLines, gridSize, linesPerPartition);

        Map<String, ExecutionContext> partitions = new HashMap<>();
        long start = 1; // 헤더를 건너뛰었으므로 1부터 시작
        long end = linesPerPartition;

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            
            // 마지막 파티션은 남은 모든 라인을 처리하도록 조정
            if (i == gridSize - 1) {
                end = totalLines;
            }

            context.putLong("startLine", start);
            context.putLong("endLine", end);
            partitions.put("partition" + i, context);
            
            log.info("Created partition{}: startLine={}, endLine={}", i, start, end);

            start = end + 1;
            end += linesPerPartition;
        }

        return partitions;
    }

    private long countLines(Resource resource) throws Exception {
        long count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }
}
