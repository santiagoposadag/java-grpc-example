package com.example.grpc.client.server.example.handlers;

import com.example.grpc.client.server.example.StudentServiceGrpc;
import com.example.grpc.client.server.example.Students.*;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@GRpcService
public class GrpcStudentsServer extends StudentServiceGrpc.StudentServiceImplBase {

    List<Student> students = new ArrayList<>();

    Logger logger = LoggerFactory.getLogger(GrpcStudentsServer.class);

    public GrpcStudentsServer(){
        Student santi = Student.newBuilder().setId(1).setName("Santiago").build();
        Student camila = Student.newBuilder().setId(2).setName("Camila").build();

        students.add(santi);
        students.add(camila);

    }

    public void findStudentById(StudentId request, StreamObserver<Student> responseObserver) {
        logger.info("received the request to find the student with the id: " + request.getId());
        Student studentResponse = students.stream().filter(student -> student.getId() == request.getId()).findFirst().orElse(null);
        responseObserver.onNext(studentResponse);
        responseObserver.onCompleted();
        logger.info("Finished the process of finding the student");
    }



    public void findAllStudents(Empty empty, StreamObserver<Student> responseObserver) {
        logger.info("received the request to return all the students");
        students.forEach(student -> {
            responseObserver.onNext(student);
        });
        responseObserver.onCompleted();
        logger.info("finished returning all the students");
    }



    public StreamObserver<Student> saveAllStudents(StreamObserver<Empty> response) {
        logger.info("got save all students request request");
        var responseStream = new StreamObserver<Student>() {
            @Override
            public void onNext(Student studentRequest) {
                logger.info("received student to store with id: " + studentRequest.getId());
                students.add(studentRequest);
                logger.info("added student");
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("got some error "+ throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("completed");
                Empty empty = Empty.newBuilder().build();
                response.onNext(empty);
                response.onCompleted();
            }
        };
        logger.info("sent response");
        return responseStream;
    }


    public StreamObserver<StudentId> lookIfExists(StreamObserver<Acknowledge> responseObserver) {
        logger.info("got request");
        var responseStream = new StreamObserver<StudentId>() {
            @Override
            public void onNext(StudentId studentRequest) {
                int studentId = studentRequest.getId();
                logger.info("received student to store with id: " + studentId);
                Student studentFound = students.stream()
                        .filter(student -> student.getId() == studentRequest.getId())
                                .findFirst().orElse(null);
                if(studentFound != null){
                    Acknowledge ack = Acknowledge.newBuilder().setId(studentId).setExists(true).build();
                    responseObserver.onNext(ack);
                    logger.info("Student with id: " + studentId + "exists: " + ack);
                }else{
                    Acknowledge ack = Acknowledge.newBuilder().setId(studentId).setExists(false).build();
                    responseObserver.onNext(ack);
                    logger.info("Student with id: " + studentId + "exists: " + ack);
                }

            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("got some error "+ throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("completed");
                responseObserver.onCompleted();
            }
        };
        logger.info("sent response");
        return responseStream;
    }
}
