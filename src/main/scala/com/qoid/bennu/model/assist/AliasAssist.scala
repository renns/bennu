package com.qoid.bennu.model.assist

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model.Profile
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.model.id.PeerId
import com.qoid.bennu.security.Role

@Singleton
class AliasAssist @Inject()(
  connectionAssist: ConnectionAssist,
  labelAssist: LabelAssist
) {

  private val anonymousAliasName = "Anonymous"
  private val anonymousAliasImage = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAlgCWAAD/4Rq1RXhpZgAATU0AKgAAAAgABwESAAMAAAABAAEAAAEaAAUAAAABAAAAYgEbAAUAAAABAAAAagEoAAMAAAABAAIAAAExAAIAAAALAAAAcgEyAAIAAAAUAAAAfodpAAQAAAABAAAAkgAAANQAAACWAAAAAQAAAJYAAAABR0lNUCAyLjguMgAAMjAxNDowMjoyMyAxMDoxNDo1OQAABZAAAAcAAAAEMDIxMKAAAAcAAAAEMDEwMKABAAMAAAABAAEAAKACAAQAAAABAAAAnqADAAQAAAABAAAAxAAAAAAABgEDAAMAAAABAAYAAAEaAAUAAAABAAABIgEbAAUAAAABAAABKgEoAAMAAAABAAIAAAIBAAQAAAABAAABMgICAAQAAAABAAAZewAAAAAAAACWAAAAAQAAAJYAAAAB/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCACeAH8DASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+k/GlrzLxlrWp+IPFNp4M8O3bW0rAy3t5EwJgjUjkEHIbORg+tAHY6r4u0HRovMvdTt4wRkDeCWypYY+oHH4Vx1z8ZtLnd4fD+mX+szDIH2eJiuduVyQOmeDnGPetXR/hL4Q0rY76aNQnUAebfMZifwPH6V2cFtBaxLFbwxxRqMKkahQPwFAHmz+I/iTrBYab4TSwjO4JJe3KqQDtwSvPIw3fnd04pX8OfE/UWY3PibS7BH3fJbRPJtBYHHOOmMdema9MpaAPNP+Fb+J7gk3vxCvnDEllhtQg5ffxlj36e3HSnj4WXu1AfG+t/Jt24WLjClR/D/dYj8a9IooA82Hwu1SIZt/HWsIwwFLRxkDCFB0A/hJH/16YfAXji2Jaz+ILuecLPY8D5NvUOe3t15616ZRQB5k9h8WbBWaLUdE1Dg4B3RtyoA6jHGM9epNI3jH4g6YJDqHghrgDcQ1nOsnpjjjgc+uc9q9OpKAPNV+M2kW8qx6tpmo6WzMVAuoSv8AEAOSAOhyeeMY5rvNL1jT9ZtUudPu4543XepVuSuSM49Mg/lVm5tbe8geC5gjmicYaORAysPcGvMPEPgu88FzyeJfBJkijjzJeaSrHy5Bggui5xuAJODxQB6rRWJ4V8S2XivQodTsm+Rx8yEglD6HHGcY4rboAhu51tbOa4cqqxoWJbOBgd8ZOK84+ENs+oQaz4suiXn1W6KROzFyIY+FAYgZGcnoM8Vu/E/VH0n4f6rcI7o7RFFZC4IJ46ryPx4rR8FaV/YngrRtPK7XhtU3j0cjLfqTQBvUV4Z8TfjhJpd9NovhcxtPCSk96wDKrd1QdCR6mvGpviD4wu7nzpfFGqqxOflunRR/wFTgflQB9sUtfLXhL4j/ABBilRbXV7TV1H/LvdzpvI9t2H/nXt3h74hpfyJZa/pN5oWonA23SHypD/sydPwOPxoA7eikzxRQAtFFU9T1Wy0eya7v7hYYV4yeST6AdSfYUAXKSvHvFPxO8VS2zv4b8OyWdj0Go6riIN7qrEAfjz7V4trPj/xpPOxufFtyX/uWdyUX8PLwv60AfZWaCARgjivjzQPi94z0K5R/7YnvoQfmhvWMoYfU8j8DX0x4C8ead480P7baAxXMR2XNsx+aNv6qex/rQBymnI3gP4tPpMZddG19TcW0YDMsUw++oGdq9NxOOmK9Wrzr4y6eX8JQa3FGrXGj3cV0u5Aw27gGBB4I5HHtXc6Tfpqek2t5GSVmjVuSpPTvtJGfoaAOA+M6m60PSdMwcX2owwn5GxywBBYcDgng5zzjpXReP73VrHwhcx6FbTT6pc4gt1hXJUt1b2wMnNc98Q1Fz468E2pj6328sY+wBOA2fUAkY9Oa9KoA+UrP4CeOb1DJNDY2pPOLi5yT/wB8hqqan8D/AB1pyNIumxXiKMk2s6sfyOCfyr64FFAHwVeWF5pl01ve201tOh5SVCrD867fwb8WNc8MFLO7b+1NI+61pdHdtH+wT0+nSvqPxJ4R0PxZYta6xYRTjHyyYxJGfVW6ivmD4j/CrUvA05u4C13o8jYS4A+aM9lcdvr0NAH074S8UaP4r0KG+0aUGAAK0JGHhIH3WHbH5VvV4N8APCzRRnxLba0zQSxvb3FgFxiQHgk5545BxnmveaAEryL4nfF3TPDNy2naTBDfa3ECpldQyW2e2e7ew/GvVdQt5bvTbm2guGt5ZYmRJl6oSMAj6V8ZeL/C0mh+MpdAtrx9VvgyiR0jOWlbnaOSScEZ980AZeueJNY8SXzXmr6hPdSseN7fKvsq9APYVc0HwR4l8TYOkaRcXEZ/5a7dqf8AfRwK9x+HXwLs9Ohh1PxXEl1en5lsicxxem7+8fbp9a9phhit4lihjSONRhURcAD2FAHyzB+z742mi3v/AGbA2M7JLkk/+OqR+tbPgfwX45+G3jO0v7nTHl0yVhDeNayCRfLPG4gc/KeenavpGkNAGV4l09dW8Larp7DIubSWL8SpArm/hFqTal8OdOaRt0sAML5ZSQR6henBHB59a7hl3IynoRivNvgzJt0LV7IuD9k1OeMLuB2jeT0ABHXuTn2xQAeLY/N+MfgwbPurO27aozhRxnOTjJ4IAGeCcnHoMN9FNe3FoMrNAFZlPdW6MPbgj8DXmfjYX/8AwtbQm01IzfCwuza+YECmQIMAkHcRn+9genesi/8AEniyz0fTbi6gU+M4HeNhCgdJYif9UyJnzDwD8n3TjJGeQD1LxR4o07wjokuqalIQi/LHGvLyueiqPU147rPjXXQDd+JvGA8OJKN8Oj6ZAs1yqnpvb+E49SPwPFYmst8Qb/xBY+JPGPhy+fTLPJSC0VcQZH3wuWOQcHLelcNfaDot/dPNZeL7dpJWLMmpwywyA/7TBWXP40Adf/wtL7NL/oHi/wAUDHR7mCGYH6q1dFpvxklurOS18QQWWu6TImy4a3jMU6KepeJuCPdSQPUV5J/wiJ/6GDQAPX7cP8M0sPhfzpxb2Gopqd8TtSDTYZJCT7sVUAe4zQB7x8NrO18Ja7qR0u5N34W1W1a/sJwfumP78Z77gG78/LXr8MqzwpKhyrqGU+xryzwb4Zv/AAL8IdUTWpFFwYZrjyshhb5QjGfX1xXfeFJTN4Q0aVjlnsYWJ9coKAJ9V1eDSod0x+ZkkdR67FLH+X6143oEGk+AFk8YeLFe58U6xI81rZIu+VAxzgDoDg8k9Olei+JbxYPHXg+3kxsuJLpCD0P7ngfnXj/xp8Ka+vjWXXzJN/ZE8SRJcRKWFthQCrAcgE5OfegDT1j40ax5zK95puiL2hWI3tyP97GEU+xOfas60+KF1eOq/wDCzLu0kPQ3Whw+Xn32AkV5cnhK7uNpstS0e73dAuoRxN/3zKVb9KefBOrxkG4l0q3QnG+XVbYD9JCaAPobw/8AEfU9P1K0svFklhcaffNss9b09swO/ZXA+6fy/LmvVQwIBBBBr4+06wsNJ069sbjX11OW8iKJpmlo0waXHyOXICgq2D8uTxjoTXcaJ47+I/h7w/DpGp6FcRxnEcOo3sLIYl9CWwpIHTcR2zQB9B293FdGYQtuEMhiZu24AZA9cZx9QR2rzz4VlY9U8YW6ycJq8hEe/pnHO3HH1zz7Y5IrzxI934ai8HLFJoaOF1X7UFEy5bLFwxzkgkgrnJOelHwvP/E88afNx/a7nbubjgfw/d7dRz69qAG+KT5fxn8IMAMvDcL/AAddo9Bu/Pj0713Wm6Pbae8lxgS3k3M1ww+Zu+B6L6Af/XrhfHWyD4leCbksuftJiwXTIDK3bG7sOScdMYOc+l0AGKwdR8E+GNWkMt/oNhPIf4mgXP51vd6WgDj4fhb4IgYsvhuxJJz86bv510en6Rp2lR+Xp9jbWqekMYX+VXKZNLHBC80sixxopZ3Y4CgdSTQB5x8c9fTRvhzc2wcC41F1to177ern6YGPxFdf4N/5EjQf+wfB/wCi1r5s+Ket6p47vLrXrS3k/wCEa0yQWtvM3CuzHlh6kkfgMV9L+E4zD4O0WJgQUsYVIPsgoA81+N+rHQtV8G6qpx9lvnkP0G3P6V63BNDfWcc0ZWSCZAynqGUivF/2jLOS607w6sQy73TxKM92C4rT+D/ie8slk8B+I42tdX00EW6y8GWLrgHvjPGOq49KAOw1T4aeDdYdnvPD9oZGOS8amNj+K4rIX4IeAFk3/wBjOfY3UuP/AEKvQqWgDE0bwj4f8PY/snSLS1YDG9Ixu/76PNbDosiMjqrIwwVIyCKfRQBk2GhwaTfPJYARW0q4eDHyoc5ynoPUdO4xznifhA3nR+KLk7syaxNyS+CBx3+X8Rz69q9Hnfy7eR/7qk/pXnnwVQt4KmvDGy/ar2aUEq43AsSD83B69V4/EGgCH4uv9kl8LaiZAgt9ViLAyAZBZQflxk8E9+Pxrs7/AMT6fpXiCz0i/lFs99EXtZZGwkjKcMmf73Kn3zXNfGS0a7+HF+VLBoSsow5HIPHABz9Dgd6574v6ePFvwksNet0zNbJHeDaM4R1G8fqD/wABoA9gByMg5FNkkSNC0jqqjkljgCvksa34gs/AFjrmg67qUSQym01CH7QXEb9UcZ6Kw4+o969C+H3hbRfiV4aTUda1nW7+6jcx3drNeny1fsQABwRQB1viz4y+HdADWunSDWNSPypBaHcu73YcfgMmsma48U+PNO0Pw5rlsNIbUVlvNQEOVf7KjKFTByVLFwD36e4rvtB8EeG/DWG0rSLaCQf8tdu5/wDvo81pXOnpPqNnfoQtxbbk3Y+9G2Ny/mFP1UUAeV/GnTLbTPhzo+g6ZAtvayalDAiIOANr/wBcV63Zw/Z7KCEdI41X8hivKvFujeMde+2aXrzaXDpEc32ux1dH8v7M6ZKeYrHoc7T9c5NWLX4g+M9JVbXW/BF1esqgLd6U/mRzehx2z9aAG/H22ZvBFlfRj57LUYnz6Agj+e2t7xd4Qj8T6HaanaubXXrKJZ7O8ThgwGdreqnp+NcZ4hvPE/jqG3tdZ0m30LR0mWdbOa5U3d86crGgOAMntj867rwvd+LtX06+/t3SbTR4mUJZwo5eRVxglznH0xigDltX+KWvaFpvh/Wrjw/5+i3lqrXs0WS8UpOCB2A9M9c9RXbeGvHfhzxZAr6VqcLykZa3dgsq/VTz+I4ratdPtrTTYtPjiU20UYiCMMgqBjBrkNY+Efg3WJjcNpf2S4zkS2bmIg+vHH6UAdxmori5htLeS4uZkhhjUs8kjBVUDqSTXzV4+1i58E65FoHhPxXr11dKQJ0luVlWMnog+XJb8eKyPiBFrP2vRPDd/rF/qOt3CI95E8xKRvIfkjCDjIGCc+ooA+hNR8U2+ofDfWNfsiwt1tLkwO3G8KGAYexI4qP4Wae2nfDnSIHj2OYt7AoUPPcgk/n0PXArB+IdmPD/AMHYPD1oP3kxt7CNV6sSwzj64P516Bolium6HY2SqFEMKpgJs6D0ycfTJoAbr9kNR0C/s2GRNAykbmGeOny8/lXHfCa4XVPhumlXqb3snlsLiJwc4BPBB5HB6V6HXlvhRT4U+K+taFIpSz1iMXloSpUF04YDLEngjJzyV96APLNL0uPwR8SNU8GaySdF1lfs3mN/dY5ikHuDx+Jqp4H1y6+FHxNudN1Rilm0htrvrtK5+SQfz+hNex/GXwEfF3hxb+xjzqungvGFHMqdWT+o9/rXlVhDY/F3Tra21K6Gm+KLRRbxXki5ivgBwjejj9eeDQB9Dp4w8MyKGXxDpRBGf+PyP/Gorrxx4VsoTLP4i0wKOu26Rj+QJNfONx8AvHEUzJFBZTIOki3KgH8Dg1F/wobx3/z42v8A4FJ/jQBZ+LXxWfxhOdI0lmj0WF8luhuGHQkf3R2H41lfDrwt4t8XvdW+iavc2FpbJl5PPkSPceiAKeprn5fCstlqQs9T1C0siJNkjSFjswcE4xk/hX0N4U+IHw28I+HbbR7DWV8uJfnkMTbpHPVjx1NAHzdq8Gr6N4hng1J7iPU7SXDM7kuGHIIJ/Ag19E+Afjhouo6LDbeJbtbHU4VCPK4OybH8QI6H1HrXJ/FvVvAvje0iv9K1mBNZgwo3oyiaP+6TjgjqD9RXneg/DnXfE8rR6OLa5KruJEu1cdPvEYoA+sYPHHhS4TdH4j0vH+1dov8AMiuN+Jfxa0zw7oDw6Hf2t7qtyCkRglWRYR3ckZGR2HrXj3/ChvHf/Pja/wDgUn+NaWjfAbWku/tPii6tdM0m3HmXEgmDsUHJAxwPqenvQA74T+H4449R+I3iHL2emh5YTLz50w6tz1wTgf7R9q1fg7otz4z8e6j441NN0cEzNHu5BmboB/uqR+YrN8b6zeeL9Z0n4f8Aheza00hAggjI2mUYyJHHZQPmGeSOe9fQfhbw7aeFPDdno9mP3dumGfGC7HlmP1NAHD/EMjW/iD4R8NDBUSNfTr8p+VeFyrcMM5JHoDXp6qFUKoAUDAA7CvLvBBHib4leJPEzsGjt5PsNsu8HaicZwRnk7jkHrur1OgArzb4raPcR2tl4p0qNRqWkzCYFVVTIvdWP3jkcADua9JpkkYljaNs4YYODg0AZ+ga3Z+I9CtNWsX3QXMYcDup7qfcHivJPGXhe18E+Lz4i+yLP4Y1ZxFqtsBxA5ORKPTDcgjoc+taG+4+EfiKSRleXwlqM3zBRn7HKc/MoJJK4GWr1KWKx1zSmjkWK7sbuLBH3kkRhQBi2NrrWkwx/YbtNZ04qDGlzJtnVe22TkPx/ewf9qty1u2uAQ9rPAw6iQD+YJBrlvCsdz4VvP+EVvZHkswC+k3LnO6McmFj/AHl7eq/Q12QoAa0cbjDorA9iM1UOj6WxJOnWZJ6kwL/hV1lDKVOcEYPNfKfxVfxD4N8bXFjZ+JtYNlMguIFN/LlFbPy/e7EH8MUAfUMel6fC++KwtUb1WFQf5VaChegx9K8m+Bmm6lc+HT4k1bWNQvpbpmjginunkSNFOCcE4ySD+H1r1ugDPury7QtHZ6e80g4DSSCOP8TycfQGuO8YGPRdDn8QeKbpb42+Da6bEu23Mx+4NpyZDnu3pnArv2YKpZiAAMkntXD22mHxp4oh169UnRNOYjTIG6TydGnYemeF+maAKHwt8FXOkxXPifXRv1/VSZZMjmBG5CfX1/Adq2/iH4nPhrwvKbZTLql6fstjCoyzytwCB7ZzW/rGr2Og6VcalqM6w2sC7nc/yHqT6V5x4V0q98d+KD4z1+18uxhzHpdhMufLX+8ysPvHhgwNAHWfDvw63hrwZZWMrlp2Hmyksx+ZuScNyM9SPUmuqoFIaAFpKWigCtfWNtqVnLaXkQlt5V2uhJG4enFeV2dzqHwj1UWV6ZrrwjcsTHMPmaxbIySO0eWA65/r653qrqOnWuq2MtneQRzQyDBSRA4z2ODxxQA2eC01iwjYOskT7ZYZUOcHqrqf61bUEKATkgcn1ryOG51L4Q6gYLsy3ng2aQCJxlnsS3TnHKkgnA6fz9Xs7y2v7OK7tJkmt5lDxyIchge4NAE9fIPxq1F9Q+KWqqTlLXZbp9AoJ/Umvr6vjn4wWptfirrqkcSSrIPcMin+eaAPZP2ddRe58FXtk5JFpdnbnsGAOPzBr2KvFf2b7do/CurTnO2W7UD8F/8Ar17VQBVvrJL+2NtMzCFyPMVTjevdSfQ9/aq+ravpvhvR5b+/mjtbO3Xr0+iqO57ACqPivxhpXhDTvtWoSFpZDtgto+ZJ27Ko/r0ridH8Laz481SHxF4x3QWafPY6UjkCH0Zuh3gjvnrQBFp+nan8U9Zh1rWYWtfDNu4ksbCXrPyCJHXBVgRkYzxXq8MMVvCkMMaxxIoVEQYCgdABTo40iQIiBVHAVRgCnUAFFFFABSUtGKAEpaKKAK97ZQX9pJa3MYkikUqwPoRjg9q8suNK174X6hLfaDE+o+G5pC02lgktDnccxdcAKOcnBr1umsiyIyOoZWGCDyCKAMXw14t0bxZp4u9Ku1kwP3kLfLJEe4ZeoP6V8+/tE6YbbxtZ34XC3doATjupI/kRXr/iH4XafqOpnV9GvLjRdWJybm0YjcSRksO/AIA4HNcX4o+HnjPxTZWlrrniLTruOGZPKka22yLufYeVAyMYJ9T+dAHYfBTTv7M+Fums67HujJctn3YgH/vkCpvFPxNsdNnbSdBj/tjXCOIIPmjh/wBqRhwAM9P5VhWnw58TatZ21prnjCYafHGi/Y9PQQR7ACNvAGQPlxn3ruvDXgzRPCtusemWaI+BulPLE7QCcnpnAJxQBzXhT4fXDaifEnjCX7frkykGN8NHADkFFXlcdCCMH8zXomKWigApKWigAooooA//2f/hC15odHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvADw/eHBhY2tldCBiZWdpbj0n77u/JyBpZD0nVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkJz8+Cjx4OnhtcG1ldGEgeG1sbnM6eD0nYWRvYmU6bnM6bWV0YS8nPgo8cmRmOlJERiB4bWxuczpyZGY9J2h0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMnPgoKIDxyZGY6RGVzY3JpcHRpb24geG1sbnM6ZGM9J2h0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvJz4KICA8ZGM6Zm9ybWF0PmltYWdlL2pwZWc8L2RjOmZvcm1hdD4KIDwvcmRmOkRlc2NyaXB0aW9uPgoKIDxyZGY6RGVzY3JpcHRpb24geG1sbnM6eG1wPSdodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvJz4KICA8eG1wOkNyZWF0b3JUb29sPkFkb2JlIFBob3Rvc2hvcCBDUzMgV2luZG93czwveG1wOkNyZWF0b3JUb29sPgogIDx4bXA6Q3JlYXRlRGF0ZT4yMDA4LTAzLTEwVDIzOjI0OjEyKzEwOjAwPC94bXA6Q3JlYXRlRGF0ZT4KICA8eG1wOk1vZGlmeURhdGU+MjAwOC0wMy0xMFQyMzoyNDoxMisxMDowMDwveG1wOk1vZGlmeURhdGU+CiAgPHhtcDpNZXRhZGF0YURhdGU+MjAwOC0wMy0xMFQyMzoyNDoxMisxMDowMDwveG1wOk1ldGFkYXRhRGF0ZT4KIDwvcmRmOkRlc2NyaXB0aW9uPgoKIDxyZGY6RGVzY3JpcHRpb24geG1sbnM6eG1wTU09J2h0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8nPgogIDx4bXBNTTpJbnN0YW5jZUlEPnV1aWQ6MjJDMUFGQzlBMUVFREMxMTk2RDQ5MUI1MTk3QUM4M0Y8L3htcE1NOkluc3RhbmNlSUQ+CiAgPHhtcE1NOkRvY3VtZW50SUQgcmRmOnJlc291cmNlPSd1dWlkOjIxQzFBRkM5QTFFRURDMTE5NkQ0OTFCNTE5N0FDODNGJyAvPgogIDx4bXBNTTpJbnN0YW5jZUlEPnV1aWQ6MjJDMUFGQzlBMUVFREMxMTk2RDQ5MUI1MTk3QUM4M0Y8L3htcE1NOkluc3RhbmNlSUQ+CiA8L3JkZjpEZXNjcmlwdGlvbj4KCiA8cmRmOkRlc2NyaXB0aW9uIHhtbG5zOmV4aWY9J2h0dHA6Ly9ucy5hZG9iZS5jb20vZXhpZi8xLjAvJz4KICA8ZXhpZjpPcmllbnRhdGlvbj5Ub3AtbGVmdDwvZXhpZjpPcmllbnRhdGlvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj4xNTA8L2V4aWY6WFJlc29sdXRpb24+CiAgPGV4aWY6WVJlc29sdXRpb24+MTUwPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6U29mdHdhcmU+R0lNUCAyLjQuNjwvZXhpZjpTb2Z0d2FyZT4KICA8ZXhpZjpEYXRlVGltZT4yMDA5OjAyOjE1IDE2OjMzOjIxPC9leGlmOkRhdGVUaW1lPgogIDxleGlmOkNvbXByZXNzaW9uPkpQRUcgY29tcHJlc3Npb248L2V4aWY6Q29tcHJlc3Npb24+CiAgPGV4aWY6WFJlc29sdXRpb24+MTUwPC9leGlmOlhSZXNvbHV0aW9uPgogIDxleGlmOllSZXNvbHV0aW9uPjE1MDwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOkZsYXNoUGl4VmVyc2lvbj5GbGFzaFBpeCBWZXJzaW9uIDEuMDwvZXhpZjpGbGFzaFBpeFZlcnNpb24+CiAgPGV4aWY6T3JpZW50YXRpb24+VG9wLWxlZnQ8L2V4aWY6T3JpZW50YXRpb24+CiAgPGV4aWY6WFJlc29sdXRpb24+MTUwPC9leGlmOlhSZXNvbHV0aW9uPgogIDxleGlmOllSZXNvbHV0aW9uPjE1MDwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOlNvZnR3YXJlPkdJTVAgMi44LjI8L2V4aWY6U29mdHdhcmU+CiAgPGV4aWY6RGF0ZVRpbWU+MjAxNDowMjoyMyAxMDoxMDo0NDwvZXhpZjpEYXRlVGltZT4KICA8ZXhpZjpDb21wcmVzc2lvbj5KUEVHIGNvbXByZXNzaW9uPC9leGlmOkNvbXByZXNzaW9uPgogIDxleGlmOlhSZXNvbHV0aW9uPjE1MDwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj4xNTA8L2V4aWY6WVJlc29sdXRpb24+CiAgPGV4aWY6UmVzb2x1dGlvblVuaXQ+SW5jaDwvZXhpZjpSZXNvbHV0aW9uVW5pdD4KICA8ZXhpZjpGbGFzaFBpeFZlcnNpb24+Rmxhc2hQaXggVmVyc2lvbiAxLjA8L2V4aWY6Rmxhc2hQaXhWZXJzaW9uPgogIDxleGlmOk9yaWVudGF0aW9uPlRvcC1sZWZ0PC9leGlmOk9yaWVudGF0aW9uPgogIDxleGlmOlhSZXNvbHV0aW9uPjE1MDwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj4xNTA8L2V4aWY6WVJlc29sdXRpb24+CiAgPGV4aWY6UmVzb2x1dGlvblVuaXQ+SW5jaDwvZXhpZjpSZXNvbHV0aW9uVW5pdD4KICA8ZXhpZjpTb2Z0d2FyZT5HSU1QIDIuNC42PC9leGlmOlNvZnR3YXJlPgogIDxleGlmOkRhdGVUaW1lPjIwMDk6MDI6MTUgMTY6MzM6MjE8L2V4aWY6RGF0ZVRpbWU+CiAgPGV4aWY6Q29tcHJlc3Npb24+SlBFRyBjb21wcmVzc2lvbjwvZXhpZjpDb21wcmVzc2lvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj4xNTA8L2V4aWY6WFJlc29sdXRpb24+CiAgPGV4aWY6WVJlc29sdXRpb24+MTUwPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6RXhpZlZlcnNpb24+RXhpZiBWZXJzaW9uIDIuMTwvZXhpZjpFeGlmVmVyc2lvbj4KICA8ZXhpZjpGbGFzaFBpeFZlcnNpb24+Rmxhc2hQaXggVmVyc2lvbiAxLjA8L2V4aWY6Rmxhc2hQaXhWZXJzaW9uPgogIDxleGlmOkNvbG9yU3BhY2U+c1JHQjwvZXhpZjpDb2xvclNwYWNlPgogIDxleGlmOlBpeGVsWERpbWVuc2lvbj4xMTMxPC9leGlmOlBpeGVsWERpbWVuc2lvbj4KICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+MTYwMDwvZXhpZjpQaXhlbFlEaW1lbnNpb24+CiA8L3JkZjpEZXNjcmlwdGlvbj4KCjwvcmRmOlJERj4KPC94OnhtcG1ldGE+Cjw/eHBhY2tldCBlbmQ9J3InPz4K/+IMWElDQ19QUk9GSUxFAAEBAAAMSExpbm8CEAAAbW50clJHQiBYWVogB84AAgAJAAYAMQAAYWNzcE1TRlQAAAAASUVDIHNSR0IAAAAAAAAAAAAAAAEAAPbWAAEAAAAA0y1IUCAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA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"

  def createAlias(
    name: String,
    profileName: String,
    profileImage: String,
    parentAliasIid: Option[InternalId],
    connectionIid: InternalId = InternalId.random,
    aliasIid: InternalId = InternalId.random
  ): Alias = {
    val labelIid = InternalId.random

    val alias = Alias.insert(Alias(labelIid, connectionIid, iid = aliasIid))
    val label = Label.insert(Label(name, data = labelAssist.aliasLabelData, iid = labelIid))

    parentAliasIid.foreach { iid =>
      val parentAlias = Alias.fetch(iid)
      LabelChild.insert(LabelChild(parentAlias.labelIid, label.iid))
    }

    val metaLabel = Label.insert(Label(labelAssist.metaLabelName, data = labelAssist.metaLabelData))
    LabelChild.insert(LabelChild(label.iid, metaLabel.iid))

    val connectionsLabel = Label.insert(Label(labelAssist.connectionsLabelName, data = labelAssist.metaLabelData))
    LabelChild.insert(LabelChild(metaLabel.iid, connectionsLabel.iid))

    val verificationsLabel = Label.insert(Label(labelAssist.verificationsLabelName, data = labelAssist.metaLabelData))
    LabelChild.insert(LabelChild(metaLabel.iid, verificationsLabel.iid))

    Profile.insert(Profile(alias.iid, profileName, profileImage))

    val peerId = PeerId.random
    val connection = connectionAssist.createConnection(alias.iid, peerId, peerId, connectionsLabel.iid, connectionIid)
    LabelAcl.insert(LabelAcl(connection.iid, label.iid, Role.AliasAdmin, maxDegreesOfVisibility = 1))

    alias
  }

  def createAnonymousAlias(parentAliasIid: InternalId): Alias = {
    createAlias(anonymousAliasName, anonymousAliasName, anonymousAliasImage, Some(parentAliasIid))
  }

  def deleteAlias(aliasIid: InternalId): Unit = {
    //TODO: What does it delete?
  }
}
