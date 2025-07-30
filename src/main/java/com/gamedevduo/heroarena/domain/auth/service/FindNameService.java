package com.gamedevduo.heroarena.domain.auth.service;


import com.gamedevduo.heroarena.domain.auth.presentation.dto.request.FindNameRequest;
import com.gamedevduo.heroarena.domain.auth.presentation.dto.response.FindNameResponse;

public interface FindNameService {
    FindNameResponse findNameByEmail(FindNameRequest request);
}
