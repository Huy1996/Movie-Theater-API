package com.example.MovieTheaterAPI.showtime;

import com.example.MovieTheaterAPI.showtime.dto.ShowTimeDTO;

import java.time.LocalDate;
import java.util.List;

public interface ShowTimeService {
    ShowTime createShowTime(ShowTimeDTO showTimeDTO);
    List<ShowTime> getShowTimeByMovie(long movieId);
    List<ShowTime> getShowTimeByLocation(long locationId);

    List<ShowTime> getShowTimeByDate(LocalDate date);
}
