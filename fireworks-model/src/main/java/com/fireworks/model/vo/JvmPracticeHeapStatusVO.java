package com.fireworks.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * JVM 堆泄漏演练：当前登录用户泄漏状态（模拟「用户维度购物车缓存」误用静态 Map 的场景）。
 */
@ApiModel(value = "JvmPracticeHeapStatusVO", description = "JVM演练-堆泄漏状态")
public class JvmPracticeHeapStatusVO {

    @ApiModelProperty(value = "当前会员ID", example = "1")
    private Long memberId;

    @ApiModelProperty(value = "为该用户累积的泄漏字节数（byte[] 长度之和）", example = "524288")
    private long leakedBytesApprox;

    @ApiModelProperty(value = "追加次数（byte[] 块数）", example = "10")
    private int chunkCount;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public long getLeakedBytesApprox() {
        return leakedBytesApprox;
    }

    public void setLeakedBytesApprox(long leakedBytesApprox) {
        this.leakedBytesApprox = leakedBytesApprox;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }
}
