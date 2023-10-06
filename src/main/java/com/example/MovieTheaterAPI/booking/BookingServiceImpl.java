package com.example.MovieTheaterAPI.booking;

import com.example.MovieTheaterAPI.screen.utils.ResourceNotFoundException;
import com.example.MovieTheaterAPI.showtime.ShowTime;
import com.example.MovieTheaterAPI.showtime.ShowTimeRepository;
import com.example.MovieTheaterAPI.user.Tier;
import com.example.MovieTheaterAPI.user.User;
import com.example.MovieTheaterAPI.user.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class BookingServiceImpl implements BookingService{

    private final float ONLINE_SERVICE_FEE = 1.50f;
    private final float TAXES = 0.1f;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowTimeRepository showTimeRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository, ShowTimeRepository showTimeRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.showTimeRepository = showTimeRepository;
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(BookingNotFoundException::new);
    }

    private double getTotalPrice(BookingDTO bookingDTO, User user, ShowTime showTime){
        double totalPrice = 0;

        if(user.getMember().getMembershipTier() != Tier.Premium) {
            totalPrice += ONLINE_SERVICE_FEE * bookingDTO.getSeats().length;
        }

        for (int seat : bookingDTO.getSeats()) {
            // Get price and update seats
        }
        totalPrice = totalPrice + totalPrice * TAXES;

        return totalPrice;

    }

    // Function to check available seats
    private boolean checkAvailableSeats(BookingDTO bookingDTO, ShowTime showTime) {
        Integer[] availableSeats = showTime.getAvailableSeat();
        for (int seat : bookingDTO.getSeats()) {
            if (availableSeats[seat] == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Booking createBooking(BookingDTO bookingDTO) {
        User existingUser = userRepository.findById(bookingDTO.getUserId()).orElseThrow(ResourceNotFoundException::new);
        ShowTime existingShowTime = showTimeRepository.findById(bookingDTO.getShowTimeId()).orElseThrow(ResourceNotFoundException::new);

        if (!checkAvailableSeats(bookingDTO, existingShowTime)) {
            throw new SeatNotAvailableException();
        }

        double totalPrice = getTotalPrice(bookingDTO, existingUser, existingShowTime);

        // Update reward points
        existingUser.getMember().setRewardPoint(existingUser.getMember().getRewardPoint() + (int)totalPrice);
        userRepository.save(existingUser);
        showTimeRepository.save(existingShowTime);

        Booking booking = new Booking(existingUser, existingShowTime, bookingDTO.getSeats(), LocalDate.now(), LocalTime.now(), totalPrice, BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }

    @Override
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(BookingNotFoundException::new);
        booking.setStatus(BookingStatus.CANCELLED);
        // Update reward points
        User user = userRepository.findById(booking.getUser().getId()).orElseThrow(ResourceNotFoundException::new);
        user.getMember().setRewardPoint(user.getMember().getRewardPoint() - (int)booking.getTotalPrice());
        userRepository.save(user);

        // Update available seats
        ShowTime showTime = showTimeRepository.findById(booking.getShowTime().getId()).orElseThrow(ResourceNotFoundException::new);
        // Need code to update available seats
        showTimeRepository.save(showTime);

        // Handle refund
        // Need code to handle refund

        bookingRepository.save(booking);
    }

    @Override
    public void payBooking(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(BookingNotFoundException::new);
        booking.setStatus(BookingStatus.PAID);
        bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Override
    public List<Booking> getBookingsByAfterBookingDate(LocalDate bookingDate) {
        return bookingRepository.getBookingsByBookingDateAfter(bookingDate);
    }
}