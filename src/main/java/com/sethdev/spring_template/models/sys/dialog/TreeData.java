package com.sethdev.spring_template.models.sys.dialog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TreeData {
    private List<TreeNode> treeNodes;

    private List<Map<String, Object>> dataList;

    @Getter
    @Setter
    public static class TreeNode {
        private String id;
        private String parentId;
        private String key;
        private String label;
        private String data;
        private String icon;
        private boolean selectable;
        private List<TreeNode> children;
    }
}
