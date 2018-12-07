package academy.softserve.eschool.controller;

import academy.softserve.eschool.dto.EditUserDTO;
import academy.softserve.eschool.dto.TeacherDTO;
import academy.softserve.eschool.repository.TeacherRepository;
import academy.softserve.eschool.repository.UserRepository;
import academy.softserve.eschool.service.TeacherService;
import academy.softserve.eschool.wrapper.GeneralResponseWrapper;
import academy.softserve.eschool.wrapper.Status;
import io.swagger.annotations.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
@Api(value = "Teacher endpoints", description = "Operations with teachers")
@RequiredArgsConstructor
public class TeacherController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeacherController.class);

    @NonNull
    private TeacherRepository teacherRepository;

    @NonNull
    private UserRepository userRepository;

    @NonNull
    private TeacherService teacherService;

    @GetMapping("")
    @ApiOperation(value = "Admin gets list of teachers", extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "admin", value = "the admin is allowed to get the list of teachers")})})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 500, message = "Server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    public GeneralResponseWrapper<List<TeacherDTO>> getall() {
        return new GeneralResponseWrapper<>(Status.of(HttpStatus.OK), teacherService.getAll(teacherRepository.findAll()));
    }

    @PostMapping
    @ApiOperation(value = "Admin adds a teacher, first name and last name passed in html", extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "admin", value = "the admin is allowed to create a new teacher")})})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Teacher successfully created"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 500, message = "Server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public GeneralResponseWrapper<TeacherDTO> addTeacher(
            @ApiParam(value = "Teacher object", required = true) @RequestBody TeacherDTO teacher) {
        LOGGER.info("Creating teacher [{} {}]", teacher.getLastname(), teacher.getFirstname());
        return new GeneralResponseWrapper<>(Status.of(HttpStatus.OK), teacherService.addOne(teacher));
    }

    @ApiOperation(value = "Admin or teacher get all info about teacher", extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "admin", value = "the admin is allowed to get all info about a teacher"),
            @ExtensionProperty(name = "teacher", value = "a teacher is allowed to get all info about himself")})})
    @GetMapping("/{idTeacher}")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 500, message = "Server error")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and principal.id == #idTeacher)")
    public GeneralResponseWrapper<TeacherDTO> getTeacher(
            @ApiParam(value = "ID of teacher", required = true) @PathVariable int idTeacher) {
        return new GeneralResponseWrapper<>(Status.of(HttpStatus.OK), teacherService.getOne(teacherRepository.findById(idTeacher).get()));
    }

    @PutMapping("/{idTeacher}")
    @ApiOperation(value = "Teacher updates his profile", extensions = {@Extension(name = "roles", properties = {
            @ExtensionProperty(name = "teacher", value = "a teacher is allowed to update his profile")})})
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Teacher successfully updated"),
                    @ApiResponse(code = 400, message = "Bad request"),
                    @ApiResponse(code = 500, message = "Server error")
            }
    )
    @PreAuthorize("hasRole('TEACHER') and principal.id == #idTeacher")
    public GeneralResponseWrapper<TeacherDTO> updateTeacher(
            @ApiParam(value = "User object", required = true) @RequestBody EditUserDTO teacher,
            @ApiParam(value = "ID of teacher", required = true) @PathVariable int idTeacher) {
        return new GeneralResponseWrapper<>(Status.of(HttpStatus.OK), teacherService.updateTeacher(userRepository.findById(idTeacher).get(), teacher));
    }
}

