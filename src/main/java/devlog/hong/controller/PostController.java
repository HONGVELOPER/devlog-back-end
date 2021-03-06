package devlog.hong.controller;

import devlog.hong.controller.result.BaseResult;
import devlog.hong.controller.result.ListResult;
import devlog.hong.controller.result.SingleResult;
import devlog.hong.dto.*;
import devlog.hong.service.AwsS3Service;
import devlog.hong.service.ImageService;
import devlog.hong.service.PostService;
import devlog.hong.service.response.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ResponseService responseService;
    private final ImageService imageService;
    private final AwsS3Service awsS3Service;

    @PostMapping("/post")
    public SingleResult<PostResponseDto> save(@RequestBody @Valid PostRequestDto postRequestDto) {
        PostResponseDto postResponseDto = postService.save(postRequestDto);
        return responseService.getSingleResult(postResponseDto);
    }

    @GetMapping("/posts")
    public ListResult<PostResponseDto> findAll() {
        return responseService.getListResult(postService.findAll());
    }

    @GetMapping("/post/{postId}")
    public SingleResult<PostResponseDto> findById(@PathVariable("postId") int postId) {
        postService.updateViewCount(postId);
        return responseService.getSingleResult(postService.findById(postId));
    }

    @PutMapping("/post/{postId}")
    public SingleResult<PostResponseDto> update(@PathVariable("postId") int postId, @RequestBody PostRequestDto postRequestDto) {
        System.out.println(postRequestDto.toString() + "request dto");
        if (!postRequestDto.getDeleteImages().isEmpty()) {
            awsS3Service.delete(postRequestDto.getDeleteImages());
            imageService.delete(postRequestDto.getDeleteImages());
        }
        PostResponseDto postResponseDto = postService.update(postId, postRequestDto);
        return responseService.getSingleResult(postResponseDto);
    }

    @DeleteMapping("/post/{postId}")
    public BaseResult delete(@PathVariable("postId") int postId) {
        List<String> deleteFileNameList = postService.delete(postId);
        S3RequestDto s3RequestDto = new S3RequestDto();
        s3RequestDto.setDeleteFileNameList(deleteFileNameList);
        awsS3Service.delete(s3RequestDto.getDeleteFileNameList());
        return responseService.getSuccessResult();
    }

    @PostMapping("/post/password")
    public BaseResult password(@RequestBody @Valid PasswordRequestDto passwordRequestDto) {
        boolean response = postService.verifyPassword(passwordRequestDto.getPassword());
        return response ? responseService.getSuccessResult() : responseService.getFailResult();
    }
}
