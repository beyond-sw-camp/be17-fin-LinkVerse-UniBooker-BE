package org.example.unibooker.domain.resource.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "커스텀 필드 관리", description = "커스텀 필드에 대한 값들을 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom")
public class CustomFieldController {

}
