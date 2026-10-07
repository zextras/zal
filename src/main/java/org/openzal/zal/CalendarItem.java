/*
 * ZAL - Zextras Abstraction Layer.
 * Copyright (C) 2023 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal;

import com.zimbra.common.calendar.ParsedDateTime;
import com.zimbra.common.calendar.ZCalendar.ICalTok;
import com.zimbra.cs.mailbox.calendar.RecurId;
import com.zimbra.cs.mailbox.calendar.ZAttendee;
import java.util.Objects;

import org.openzal.zal.calendar.CalendarItemData;
import org.openzal.zal.calendar.CalendarMime;
import org.openzal.zal.calendar.Invite;
import org.openzal.zal.calendar.PlainTextToHtmlConverter;
import org.openzal.zal.calendar.RecurrenceId;
import org.openzal.zal.exceptions.ExceptionWrapper;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CalendarItem extends Item
{
  @Nonnull private final com.zimbra.cs.mailbox.CalendarItem mCalendarItem;

  public CalendarItem(@Nonnull Object item)
  {
    super((MailItem) item);
    mCalendarItem = (com.zimbra.cs.mailbox.CalendarItem) item;
  }

    public void copyReplyInfoTo(CalendarItem calendar)
  {
    try
    {
      calendar.mCalendarItem.setReplies(
        mCalendarItem.getAllReplies()
      );
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public MimeMessage getMimeMessage()
  {
    try
    {
      return mCalendarItem.getMimeMessage();
    }
    catch (ServiceException e)
    {
      throw ExceptionWrapper.wrap(e);
    }
  }

  public boolean isRecurring()
  {
    return mCalendarItem.isRecurring();
  }

  public long getStartTime()
  {
    return mCalendarItem.getStartTime();
  }

  public long getEndTime()
  {
    return mCalendarItem.getEndTime();
  }

  @Nullable
  public Invite getDefaultInviteOrNull()
  {
    // BEWARE! never call this during item restore
    return getDefaultInviteOrNull(null);
  }

  @Nullable
  public Invite getDefaultInviteOrNull(@Nullable MimeMessage mimeMessage)
  {
    return wrap(mCalendarItem.getDefaultInviteOrNull(), mimeMessage);
  }

  public List<Invite> getInvites()
  {
    com.zimbra.cs.mailbox.calendar.Invite[] inviteArray = mCalendarItem.getInvites();
    List<Invite> inviteList = new ArrayList<Invite>(inviteArray.length);
    for (com.zimbra.cs.mailbox.calendar.Invite invite : inviteArray)
    {
      inviteList.add(wrap(invite));
    }
    return inviteList;
  }

  @Nullable
  public Invite getInvite(@Nullable RecurrenceId recurId)
  {
    if(recurId == null)
    {
      return wrap(mCalendarItem.getDefaultInviteOrNull());
    }
    return wrap(mCalendarItem.getInviteForRecurId(recurId.getExceptionStartTimeUtc()));
  }

    private Invite wrap(com.zimbra.cs.mailbox.calendar.Invite invite)
  {
    return wrap(invite, null);
  }

  private Invite wrap(com.zimbra.cs.mailbox.calendar.Invite invite, MimeMessage mimeMessage)
  {
    if (invite == null)
    {
      return null;
    }

    return new Invite(invite, mimeMessage);
  }

  public void updatePartStat(
    PlainTextToHtmlConverter textParser,
    Account invitedUser,
    String partStat,
    @Nullable RecurrenceId recurId,
    long time
  )
    throws MessagingException
  {
    Mailbox mailbox = getMailbox();
    OperationContext operationContext = mailbox.newOperationContext();
    int sequence;
    Invite requestInvite;

    Invite defaultInvite = getDefaultInviteOrNull(null);
    if (recurId == null)
    {
      requestInvite = defaultInvite;
    }
    else
    {
      requestInvite = getInvite(recurId);
    }

    if (defaultInvite == null || requestInvite == null)
    {
      return;
    }

    sequence = requestInvite.getSequence();

    MimeMessage mimeMessage = null;
    if (getDigest() != null)
    {
      mimeMessage = getMimeMessage();
    }

    List<Invite> exceptions = defaultInvite.getRecurrencesInvitees(Invite.TYPE_EXCEPTION);

    List<CalendarItemData> newExceptions = new ArrayList<>(exceptions.size());
    for (Invite exception : exceptions)
    {
      if (recurId == null || !recurId.equals(exception.getRecurId())) {
        newExceptions.add(
          new CalendarItemData(
            exception,
            getParsedMessage(
              textParser,
              mailbox,
              mimeMessage,
              exception
            )
          )
        );
      }
    }

    CalendarItemData defaultCalendarItemData;
    if (recurId == null)
    {
      try {
        Invite refetchedInvite;
        CalendarItem refetchedCalendarItem;
        com.zimbra.cs.mailbox.calendar.Invite localException = defaultInvite
                .toZimbra(com.zimbra.cs.mailbox.calendar.Invite.class)
                .getCalendarItem()
                .getDefaultInviteOrNull();

        localException.setPartStat(partStat);
        localException.setMethod(ICalTok.REPLY.toString());

        ZAttendee matchingAttendee = localException.getMatchingAttendee(
            invitedUser.toZimbra(com.zimbra.cs.account.Account.class));

        if (matchingAttendee != null) {
          matchingAttendee.setPartStat(partStat);
        }
        mailbox.setCalendarItem(
            operationContext,
            getFolderId(),
            getFlagBitmask(),
            getTags(),
            new CalendarItemData(
                this.getDefaultInviteOrNull(),
                this.getParsedMessage(
                    textParser,
                    mailbox,
                    mimeMessage,
                    this.getDefaultInviteOrNull()
                )
            ),
            newExceptions,
            null,
            0L
        );
      } catch (ServiceException e) {
        throw ExceptionWrapper.wrap(e);
      }
    }
    else
    {
      try {
        Invite refetchedInvite;
        CalendarItem refetchedCalendarItem;
        com.zimbra.cs.mailbox.calendar.Invite localException = defaultInvite.toZimbra(com.zimbra.cs.mailbox.calendar.Invite.class)
            .makeInstanceInvite(ParsedDateTime.fromUTCTime(recurId.getExceptionStartTimeUtc()));

        localException.setPartStat(partStat);
        localException.setMethod(ICalTok.REPLY.toString());

        ZAttendee matchingAttendee = localException.getMatchingAttendee(
            invitedUser.toZimbra(com.zimbra.cs.account.Account.class));

        if (matchingAttendee != null) {
          matchingAttendee.setPartStat(partStat);
        }

        Invite newInvite = new Invite(localException);
        newInvite.setMethod(ICalTok.REPLY.toString());

        MimeMessage mmInv = mCalendarItem.getSubpartMessage(defaultInvite.getMailItemId());

        mailbox.addInvite(
            operationContext,
            newInvite,
            mCalendarItem.getFolderId(),
            mmInv
        );

        refetchedCalendarItem = mailbox.getCalendarItemById(operationContext, this.getId());

        refetchedInvite = refetchedCalendarItem.getInvite(recurId);

        if (refetchedInvite != null) {
          refetchedInvite.getMatchingAttendee(invitedUser);
        }

        if (matchingAttendee != null && Objects.equals(matchingAttendee.getCn(), mailbox.getAccount().getCn())) {
          mailbox.modifyPartStat(
                  operationContext,
                  refetchedCalendarItem.getId(),
                  recurId,
                  invitedUser.getCn(),
                  matchingAttendee.getAddress(),
                  null,
                  matchingAttendee.getRole(),
                  partStat,
                  Boolean.FALSE,
                  refetchedCalendarItem.getModifiedSequence(),
                  time
          );
        }

      } catch (ServiceException e) {
        throw ExceptionWrapper.wrap(e);
      }
    }


  }

  @Nullable
  public ParsedMessage getParsedMessage(
    PlainTextToHtmlConverter textParser,
    Mailbox mailbox,
    MimeMessage mimeMessage,
    Invite invite
  )
  {
    ParsedMessage parsedMessage;
    if (mimeMessage == null)
    {
      return null;
    }

    parsedMessage = new ParsedMessage(mimeMessage, mailbox.attachmentsIndexingEnabled());

    try
    {
      CalendarMime calendarMime = new CalendarMime(textParser);
      MimeMessage newMimeMessage = calendarMime.createCalendarMessage(
        invite,
        parsedMessage.getMimeMessage()
      );

      parsedMessage = new ParsedMessage(
        newMimeMessage,
        mailbox.attachmentsIndexingEnabled()
      );
    }
    catch (Exception ex)
    {
      return null;
    }

    return parsedMessage;
  }

    public boolean isPublic()
  {
    return mCalendarItem.isPublic();
  }

  private com.zimbra.cs.mailbox.calendar.Invite matchingInvite(RecurId recurId) {
    com.zimbra.cs.mailbox.calendar.Invite[] invites = mCalendarItem.getInvites();
    if (invites == null) {
      return null;
    }
    for (com.zimbra.cs.mailbox.calendar.Invite invite : invites) {
      if ((invite.getRecurId() != null && invite.getRecurId().equals(recurId)) ||
          (invite.getRecurId() == null && recurId == null)) {
        return invite;
      }
    }
    return null;
  }

  public Invite matchingInvite(long recurrence) {
    com.zimbra.cs.mailbox.calendar.Invite matchingInvite = matchingInvite(
        new RecurrenceId(recurrence).toZimbra(RecurId.class)
    );
    if (matchingInvite == null) {
      return null;
    }
    return new Invite(matchingInvite);
  }
}
