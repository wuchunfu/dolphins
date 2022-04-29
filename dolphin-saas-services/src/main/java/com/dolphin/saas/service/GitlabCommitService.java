package com.dolphin.saas.service;

import com.dolphin.saas.entity.GitlabCommits;

import java.util.ArrayList;
import java.util.Map;

public interface GitlabCommitService {
    // 推送commit数据
    Map<String, Object> pushGitlabcommits(ArrayList<GitlabCommits> gitlabCommitsArrayList);
}
