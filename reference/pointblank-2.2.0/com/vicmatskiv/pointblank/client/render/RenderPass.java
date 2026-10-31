/*
 * Decompiled with CFR 0.152.
 */
package com.vicmatskiv.pointblank.client.render;

import java.util.ArrayDeque;
import java.util.Deque;

public enum RenderPass {
    MAIN_ITEM,
    HANDS,
    RETICLE,
    MUZZLE_FLASH,
    GLOW,
    ATTACHMENTS,
    AMMO_COUNTER,
    PIP,
    PIP_MASK,
    PIP_MASK_FAR,
    PIP_OVERLAY,
    SEE_THROUGH,
    SEE_THROUGH_MASK,
    SEE_THROUGH_MASK_FRONT,
    SEE_THROUGH_OVERLAY;

    private static Deque<StackFrame> renderPassStack;

    public static void push(RenderPass renderPass) {
        renderPassStack.addFirst(new StackFrame(renderPass));
    }

    public static RenderPass pop() {
        StackFrame stackFrame = renderPassStack.removeFirst();
        return stackFrame != null ? stackFrame.renderPass : null;
    }

    public static RenderPass current() {
        StackFrame stackFrame = renderPassStack.peekFirst();
        return stackFrame != null ? stackFrame.renderPass : null;
    }

    public static Object getEffectId() {
        StackFrame stackFrame = renderPassStack.peekFirst();
        return stackFrame != null ? stackFrame.effectId : null;
    }

    public static void setEffectId(Object effectId) {
        StackFrame stackFrame = renderPassStack.peekFirst();
        if (stackFrame == null) {
            throw new IllegalStateException("RenderPass stack is empty");
        }
        stackFrame.effectId = effectId;
    }

    static {
        renderPassStack = new ArrayDeque<StackFrame>();
    }

    private static class StackFrame {
        RenderPass renderPass;
        Object effectId;

        public StackFrame(RenderPass value) {
            this.renderPass = value;
        }
    }
}

